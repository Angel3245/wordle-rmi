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
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Random;
import java.rmi.registry.LocateRegistry;

public class Bot {

    public static void main(String[] args) {

        initializeColor(); // activamos los colores ansi en la terminal (WINDOWS)

        String[] palabras = new String[5]; // palabras que he probado
        String[] colores = new String[5]; // colores asociados a las palabras probadas devueltos por el servidor
        int numPalabras = 0;

        String ipServer = args[0]; // ip del servidor a donde quiero conectarme
        String port = args[1]; // puerto del servidor a donde quiero conectarme
        String hostname = args[2]; // user para conectarme
        String language = args[3]; // idioma escogido

        ArrayList<String> diccionario; // palabras que puede elegir el bot segun su idioma
        int estrategia = Integer.parseInt(args[4]); // establecemos cual sera la estrategia que seguira el bot

        String palabra = null; // palabra propuesta por el jugador
        boolean fin; // indicador de fin de partida
        String seguirJugando; // indicador de fin de partidas
        Respuesta respuesta = new Respuesta(); // respuestas que llegan del Server

        System.out.println("IP del servidor al que me conecto: " + ipServer + "\nPuerto: " + port);

        try {

            Registry registry = LocateRegistry.getRegistry(); // localizar el registro

            // buscar el servidor creado y obtener el objeto exportado
            Wordle obj = (Wordle) registry.lookup("//" + ipServer + ":" + port + "/Wordle");
            // Wordle obj = (Wordle) Naming.lookup("//" + ipServer + ":" + port +"/Wordle");

            // Verificamos que el idioma introducido es uno de los contemplados en el juago
            if (obj.languageSupported(language)) {
                diccionario = leerArchivo(language); // palabras que puede elegir el bot segun su idioma

                do {
                    fin = false;
                    obj.iniciarPartida(hostname, language); // inicio partida con mi user e idioma escogido

                    System.out.println("\n db   d8b   db  .d88b.  d8888b. d8888b. db      d88888b");
                    System.out.println(" 88   I8I   88 .8P  Y8. 88  `8D 88  `8D 88      88'");
                    System.out.println(" 88   I8I   88 88    88 88oobY' 88   88 88      88ooooo");
                    System.out.println(" Y8   I8I   88 88    88 88`8b   88   88 88      88~~~~~");
                    System.out.println(" `8b d8'8b d8' `8b  d8' 88 `88. 88  .8D 88booo. 88.");
                    System.out.println(
                            "  `8b8' `8d8'   `Y88P'  88   YD Y8888D' Y88888P Y88888P  by Jose Angel y Cristina:\n");

                    System.out.print(" Introduzca una palabra (exit para salir): ");

                    // se escoge una palabra siguiendo la estrategia establecida
                    palabra = escogerPalabra(diccionario, estrategia, palabras, colores, numPalabras);
                    System.out.println(palabra);

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

                        muestraTerminal(palabras, colores, numPalabras); // mostrar palabras probadas hasta el momento
                                                                         // con sus colores
                        System.out.println(respuesta.msg); // mostrar mensaje del servidor

                        // si se acaba la partida
                        if (respuesta.msg.contains("Ha acertado!!!")
                                || respuesta.msg.contains("Se han superado el numero maximo de intentos permitidos")
                                || respuesta.msg.contains("Se le han acabado los intentos")) {
                            fin = true;
                        } else {
                            // si no se ha acabado la partida, se espera un poco y se calcula la siguiente
                            // palabra
                            try {
                                Thread.sleep(2000);
                            } catch (InterruptedException e) {
                                System.out.println(e.getMessage());
                            }
                            System.out.print(" Introduzca una palabra (exit para salir): ");
                            palabra = escogerPalabra(diccionario, estrategia, palabras, colores, numPalabras);
                            System.out.println(palabra);
                        }
                    }

                    // eliminamos las palabras y los colores de intentos almacenados
                    palabras = new String[5];
                    colores = new String[5];

                    System.out.print(" Desea empezar nueva partida? (S/N) : n\n"); // Se le pregunta si se quiere seguir
                                                                                   // jugando

                    seguirJugando = "N";
                } while (!seguirJugando.equals("N"));
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
            // } catch (MalformedURLException e) { // si se emplea "Naming"
            // System.err.println(e.getMessage());
        }

    }

    // Mostrar la situacion actual de la partida
    public static void muestraTerminal(String[] palabras, String[] colores, int numPalabras) {
        // System.out.println(ansi().eraseScreen()); // limpiamos la pantalla
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

    // El bot escoge una palabra de las que conoce segun la estrategia establecida
    private static String escogerPalabra(ArrayList<String> diccionario, int estrategia, String[] palabras,
            String[] colores, int numPalabras) {
        String toret = null;
        Random rand = new Random();

        switch (estrategia) {
            case 0: // el bot devuelve siempre la misma palabra
                toret = diccionario.get(0);
                break;
            case 1: // el bot devuelve palabras que contengan alguna letra de las que sabemos que
                    // estan
                ArrayList<Character> letrasValidas = new ArrayList<>(5);

                for (int i = 0; i < numPalabras; i++) { // bucle para iterar por palabras
                    String palabra = palabras[i];
                    String color = colores[i];
                    for (int j = 0; j < 5; j++) { // bucle para iterar por letras
                        // Si la letra no esta incluida ya en el arraylist y es de color verde o
                        // amarillo la anadimos
                        if (!letrasValidas.contains(palabra.charAt(j))
                                && (color.charAt(j) == 'v' || color.charAt(j) == 'a')) { // Se sabe que una letra esta
                                                                                         // en la palabra
                            letrasValidas.add(palabra.charAt(j));
                        }
                    }
                }

                do {
                    toret = diccionario.get(rand.nextInt(diccionario.size() - 1)); // Escogemos una palabra aleatoria
                                                                                   // hasta encontrar una que coincida
                } while (!contieneCaracteres(toret, letrasValidas.toString().replaceAll("[,\\s\\[\\]]", "")));
                // Comprobamos si la palabra contiene todas las letras amarillas o verdes que se
                // han encontrado

                break;
            case 2: // el bot devuelve palabras que tengan las letras en la posicion de verdes que
                    // conocemos
                char[] letrasCorrectas = new char[5];
                for (int i = 0; i < letrasCorrectas.length; i++) {
                    letrasCorrectas[i] = '-';
                }

                for (int i = 0; i < numPalabras; i++) { // bucle para iterar por palabras
                    String palabra = palabras[i];
                    String color = colores[i];
                    for (int j = 0; j < 5; j++) { // bucle para iterar por letras
                        if (color.charAt(j) == 'v') {
                            letrasCorrectas[j] = palabra.charAt(j); // Se sabe que una letra esta en la posicion
                                                                    // correcta
                        }
                    }
                }

                do {
                    toret = diccionario.get(rand.nextInt(diccionario.size() - 1)); // Escogemos una palabra aleatoria
                                                                                   // hasta encontrar una que coincida
                    // letras = new String(letrasCorrectas).trim(); //Eliminamos espacios
                } while (!contieneCaracteresMismaPosicion(toret, letrasCorrectas)); // Comprobamos si la palabra
                                                                                    // contiene todas las letras verdes

                break;
            case 3: // el bot devuelve una palabra aleatoria
                toret = diccionario.get(rand.nextInt(diccionario.size() - 1));
                break;
        }

        return toret;
    }

    // Funcion para cargar un diccionario de un archivo en un ArrayList
    public static ArrayList<String> leerArchivo(String idioma) {

        ArrayList<String> toret = new ArrayList<>();

        // archivo con las palabras de un idioma
        File archivo = new File("rmiserver/Diccionarios/" + idioma + ".txt.midic");

        // si el archivo existe
        if (archivo.exists()) {

            try {

                FileReader fr = new FileReader(archivo); // archivo a leeer
                BufferedReader br = new BufferedReader(fr); // buffer a leer

                // Lectura del fichero
                String linea;

                while ((linea = br.readLine()) != null) {
                    // anado a las palabras de un idioma cada una de las lineas del archivo
                    toret.add(linea);
                }

                // cierro el archivo
                fr.close();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return toret;
    }

    // Funcion que devuelve si un string s1 contiene todos los caracteres de otro
    // string s2
    private static boolean contieneCaracteres(String s1, String s2) {
        for (char c : s2.toCharArray()) {
            if (s1.indexOf(c) == -1)
                return false;
        }
        return true;
    }

    // Funcion que devuelve si un string s1 contiene todos los caracteres de un
    // char[] s2 en la misma posicion
    private static boolean contieneCaracteresMismaPosicion(String s1, char[] s2) {
        char caracter;

        for (int i = 0; i < s2.length; i++) {
            caracter = s2[i];
            if (caracter != '-' && s1.charAt(i) != caracter)
                return false;
        }
        return true;
    }
}