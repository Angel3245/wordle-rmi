// Copyright (C) 2022  Jose Ángel Pérez Garrido - Cristina Outeiriño Cid
// 
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.

// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.

// You should have received a copy of the GNU General Public License
// along with this program.  If not, see <https://www.gnu.org/licenses/>.

package rmiclient;

import lib.org.fusesource.jansi.AnsiConsole;
import static lib.org.fusesource.jansi.Ansi.*;
import static lib.org.fusesource.jansi.Ansi.Color.*;

import rmiinterface.Wordle;
import rmiinterface.Respuesta;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.util.Scanner;

import java.rmi.registry.LocateRegistry;

public class Client {

    public static void main(String[] args) {

        initializeColor(); // activamos los colores ansi en la terminal (WINDOWS)

        String[] palabras = new String[5]; // palabras que he probado
        String[] colores = new String[5]; // colores asociados a las palabras probadas devueltos por el servidor
        int numPalabras = 0;

        String ipServer = args[0]; // ip del servidor a donde quiero conectarme
        String port = args[1]; // puerto del servidor a donde quiero conectarme
        String hostname = args[2]; // user para conectarme
        String language = args[3]; // idioma escogido

        String palabra = null; // palabra propuesta por el jugador
        boolean fin; // indicador de fin de partida
        Respuesta respuesta = new Respuesta(); // respuestas que llegan del Server
        String contestacion = null;

        System.out.println("IP del servidor al que me conecto: " + ipServer + "\nPuerto: " + port);

        try {

            Registry registry = LocateRegistry.getRegistry(ipServer, Integer.parseInt(port)); // localizar el registro
            // buscar el servidor creado y obtener el objeto exportado
            Wordle obj = (Wordle) registry.lookup("//" + ipServer + ":" + port + "/Wordle");

            // Verificamos que el idioma introducido es uno de los contemplados en el juago
            if (obj.languageSupported(language)) {

                do {
                    fin = false;
                    if (obj.iniciarPartida(hostname, language)) { // inicio partida con mi user e idioma escogido
                        numPalabras = 0;

                        System.out.println("\n db   d8b   db  .d88b.  d8888b. d8888b. db      d88888b");
                        System.out.println(" 88   I8I   88 .8P  Y8. 88  `8D 88  `8D 88      88'");
                        System.out.println(" 88   I8I   88 88    88 88oobY' 88   88 88      88ooooo");
                        System.out.println(" Y8   I8I   88 88    88 88`8b   88   88 88      88~~~~~");
                        System.out.println(" `8b d8'8b d8' `8b  d8' 88 `88. 88  .8D 88booo. 88.");
                        System.out.println(
                                "  `8b8' `8d8'   `Y88P'  88   YD Y8888D' Y88888P Y88888P  by Jose Angel y Cristina:\n");

                        System.out.print(" Introduzca una palabra (exit para salir): ");
                        palabra = solicitarRespuestaUser("EXIT");
                        System.out.println();

                        // si se acaban los intentos, se acierta la palabra o la palabra es "exit" se
                        // acaba la partida
                        while (!fin && !palabra.equals("EXIT")) {

                            respuesta = obj.recibirPalabra(hostname, palabra); // envio la palabra al server

                            if (!respuesta.error) { // si la palabra no da errores
                                // guardo la palabra con sus colores
                                palabras[numPalabras] = palabra;
                                colores[numPalabras] = respuesta.color;
                                numPalabras++;
                            }

                            muestraTerminal(palabras, colores, numPalabras); // mostrar palabras probadas hasta el
                                                                             // momento
                                                                             // con sus colores
                            System.out.println(respuesta.msg); // mostrar mensaje del servidor

                            // si se acaba la partida
                            if (respuesta.msg.contains("Ha acertado!!!")
                                    || respuesta.msg.contains("Se han superado el numero maximo de intentos permitidos")
                                    || respuesta.msg.contains("Se le han acabado los intentos")
                                    || respuesta.msg.contains("No se ha iniciado partida")) {
                                fin = true;
                            } else {
                                // si no se ha acabado la partida, se le pide la siguiente palabra
                                System.out.print(" Introduzca una palabra (exit para salir): ");
                                palabra = solicitarRespuestaUser("EXIT");
                                System.out.println();
                            }
                        }

                        if (palabra.equals("EXIT")) {
                            // Aviso al servidor de que no quiero seguir jugando
                            obj.finPartida(hostname);
                        }

                        // eliminamos las palabras y los colores de intentos almacenados
                        palabras = new String[5];
                        colores = new String[5];
                    } else {
                        System.out.println("Error: El usuario ya ha iniciado una partida");
                    }

                    System.out.print(" Desea empezar nueva partida? (S/N) : "); // Se le pregunta si se quiere seguir
                                                                                // jugando
                    contestacion = solicitarRespuestaUser("N");
                    System.out.println();

                } while (contestacion.equals("S"));

            }

            else {
                System.out.println("Lo sentimos, no puede jugar con el idioma que ha introducido");
            }

        } catch (RemoteException e) {
            System.err.println("Host not reachable/communication failure: " + e.toString());
        } catch (NotBoundException e) {
            System.err.println("No object bound to that name");
        } catch (NumberFormatException e) {
            System.err.println("Se debe introducir un valor numerico");
        }
    }

    // Mostrar la situacion actual de la partida
    public static void muestraTerminal(String[] palabras, String[] colores, int numPalabras) {
        System.out.println(ansi().eraseScreen()); // limpiamos la pantalla
        for (int j = 0; j < numPalabras; j++) { // bucle para iterar por palabras
            String palabra = palabras[j];
            String color = colores[j];

            for (int i = 0; i < 5; i++) { // bucle para iterar por letras
                switch (color.charAt(i)) {
                    case 'v': // Se muestran las letras correctas en la posicion correcta
                        System.out.print(ansi().fg(GREEN).a("v: " + palabra.charAt(i) + " ").reset());
                        break;
                    case 'a': // Se muestran las letras correctas en posicion incorrecta
                        System.out.print(ansi().fg(YELLOW).a("a: " + palabra.charAt(i) + " ").reset());
                        break;
                    case 'g': // Se muestran las letras incorrectas en posicion incorrecta
                        System.out.print(ansi().fg(WHITE).a("g: " + palabra.charAt(i) + " ").reset());
                        break;
                }
            }
            System.out.println("\n");
        }
    }

    // Inicializa la libreria Jansi para ver colores en la terminal de Windows (solo
    // en algunas versiones)
    private static void initializeColor() {
        boolean isWindows = System.getProperty("os.name").startsWith("Windows");
        if (isWindows) {
            AnsiConsole.systemInstall();
        }
    }

    // Si el jugador pasa cinco minutos inactivo se entiende que ha dejado de jugar
    private static String solicitarRespuestaUser(String salir) {

        String token = null;
        int x = 300; // espera 5 minutos salvo que el usuario introduzca algo antes

        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        long startTime = System.currentTimeMillis();
        try {
            while ((System.currentTimeMillis() - startTime) < x * 1000 && !in.ready()) { // se espera a que se exceda el
                                                                                         // tiempo maximo o a que el
                                                                                         // usuario introduzca algo

                try {
                    Thread.sleep(1000); // se espera un segundo antes de volver a comprobar las condiciones para reducir
                                        // la sobrecarga
                } catch (InterruptedException e) {
                    System.out.println("ERROR");
                }
            }

            if (in.ready()) {
                token = in.readLine().toUpperCase(); // el usuario ha introducido algo, se devuelve

            } else {
                // El usuario no ha introducido nada, se devuelve el token salir correspondiente
                // ('N' o 'exit')
                System.out.println("Se le ha expulsado por inactividad");
                token = salir;
            }
        } catch (IOException e) {
            System.out.println("ERROR");
        }
        return token;

    }

}