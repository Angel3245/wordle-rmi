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

package rmiserver;

import rmiinterface.Wordle;
import rmiinterface.Respuesta;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.server.RemoteServer;
import java.rmi.server.ServerNotActiveException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;

public class Server implements Wordle {

    static HashMap<String, Jugador> jugadores = new HashMap<>(); // mapa que relaciona el identificador de un jugador
                                                                 // con su clase Jugador
    static HashMap<String, LinkedList<String>> ultimasPalabras = new HashMap<>(); // mapa para almacenar las ultimas 5
                                                                                  // palabras por idioma
    static HashMap<String, ArrayList<String>> diccionarios = new HashMap<>(); // mapa para almacenar el diccionario con
                                                                              // su idioma relacionado

    public static void main(String[] args) {
        Server obj = new Server(); // levantar el servidor

        String ipServer = args[0]; // ip del servidor
        String port = args[1]; // puerto del servidor
        String hostName = args[2]; // nombre del servidor

        System.out.println("Soy el server: " + hostName);
        System.out.println("IP del servidor: " + ipServer + "\nPuerto: " + port);

        System.setProperty("java.rmi.server.hostname", ipServer);

        try {
            Wordle stub = (Wordle) UnicastRemoteObject.exportObject(obj, 0); // objeto a exportar: Wordle

            Registry registry = LocateRegistry.getRegistry();
            // nombre por el que el cliente buscara a este servidor + objeto exportado
            registry.bind("//" + ipServer + ":" + port + "/Wordle", stub);
            System.out.println("Server ready!!");

        } catch (RemoteException e) {
            System.err.println("Host not reachable/communication failure: " + e.getMessage());
        } catch (AlreadyBoundException e) {
            System.err.println("Another object is already bound to that name");
        }

    }

    // El cliente envia al servidor una palabra para que este le devuelva los
    // colores
    // correspondientes a las posiciones que haya acertado de las letras
    public Respuesta recibirPalabra(String nombre, String palabra) throws RemoteException {

        Respuesta respuesta = new Respuesta(); // Respuesta que el servidor le va a dar al cliente

        try {
            // Identificador del jugador: ip@hostname
            String id = RemoteServer.getClientHost().concat("@").concat(nombre);

            Jugador jugador = jugadores.get(id); // Cogemos el jugador asociado al identificador

            if (jugador == null) { // si el jugador no existe
                respuesta.error = true;
                respuesta.color = null;
                respuesta.msg = "No se ha iniciado partida";
            } else {
                char[] palabraJugada = jugador.palabra; // Palabra que el jugador comenzo a jugar

                palabra = palabra.toUpperCase(); // formateamos la palabra introducida
                System.out.println(
                        "Recibida la palabra " + palabra + " de " + id + " que esta jugando en " + jugador.language);
                // convertimos el String de la palabra a un char[] para facilitar las
                // comparaciones
                char[] letras = palabra.toCharArray();

                // array para almacenar el color que le corresponda a cada letra de la palabra
                // introducida
                char[] colores = new char[5];

                // Comprobamos que la longitud de la palabra sea 5 y solo contenga caracteres
                if (palabra.length() != 5 || !palabra.matches("[A-Za-z]{5}")) {
                    respuesta.error = true;
                    respuesta.color = null;
                    respuesta.msg = "La palabra debe ser de 5 caracteres";
                }

                // Comprobamos si la palabra existe en el diccionario asociado al idioma en el
                // que se esta jugando
                else if (!diccionarios.get(jugador.language).contains(palabra)) {
                    respuesta.error = true;
                    respuesta.color = null;
                    respuesta.msg = "La palabra no existe en el diccionario";
                }

                // Comprobamos si se ha superado el numero maximo de intentos permitido
                else if (jugador.intentos == 0) {
                    respuesta.error = true;
                    respuesta.color = null;
                    respuesta.msg = "Se han superado el numero maximo de intentos permitidos";
                }

                // Comprobamos si es la palabra correcta
                else if (Arrays.equals(letras, palabraJugada)) {
                    respuesta.error = false;
                    respuesta.color = "vvvvv";
                    respuesta.msg = "Ha acertado!!!";
                    jugadores.remove(id);

                    // en caso contrario comprobamos el color de las letras correspondiente
                } else {
                    respuesta.error = false;

                    // para cada letra comprobamos su color (v: bien colocada, a: mal colocada, g:
                    // no existe)
                    for (int i = 0; i < 5; i++) {
                        // Si la letra esta en la posicion correcta en la palabra jugada
                        if (letras[i] == palabraJugada[i]) {
                            colores[i] = 'v';
                            // Si la letra esta en la palabra jugada
                        } else if (new String(palabraJugada).indexOf(letras[i]) != -1) {
                            colores[i] = 'a';
                            // Si no esta
                        } else {
                            colores[i] = 'g';
                        }
                    }
                    respuesta.color = new String(colores);

                    // Actualizamos los intentos del jugador
                    jugadores.get(id).setIntentos(--jugador.intentos);
                    if (jugadores.get(id).intentos == 0) {
                        respuesta.msg = "Se le han acabado los intentos. La palabra era: "
                                + new String(jugador.palabra);
                        jugadores.remove(id);
                    } else {
                        respuesta.msg = "Le quedan " + jugadores.get(id).intentos + " intentos";
                    }
                }
            }

        } catch (ServerNotActiveException e) {
            e.printStackTrace();
        }

        return respuesta;
    }

    // Cuando el Cliente solicita iniciar una partida el Servidor lo anade a su
    // lista de Jugadores y comprueba si
    // va a jugar en un idioma no cargado para inicializarlo
    public boolean iniciarPartida(String hostname, String language) throws RemoteException {

        try {
            String id = RemoteServer.getClientHost().concat("@").concat(hostname); // identificador: id@hostname
            // Si el jugador no esta en el array de jugadores lo anadimos
            if (!jugadores.containsKey(id)) {
                jugadores.put(id, new Jugador(id, language));
            } else { // En caso de que ya este no es posible iniciar partida
                return false;
            }

            System.out.println("El cliente " + id + " ha iniciado una partida");

            // Si el usuario quiere jugar en un idioma no cargado lo cargamos
            if (!diccionarios.containsKey(language)) {
                leerArchivo(language); // leemos el diccionario de archivo correspondiente al idioma

                // creamos un array para almacenar las ultimas palabras de ese idioma
                ultimasPalabras.put(language, new LinkedList<>());

                // Iniciamos un timer para cambiar la palabra cada cinco minutos
                Hilo objetoRunnable = new Hilo(language);
                Thread hilo = new Thread(objetoRunnable, "Timer");
                hilo.start();
            }

            // Esperamos un poco para que haya tiempo para que se genere una palabra
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
            }

            // Establecemos la palabra como la palabra que esta jugando el jugador
            jugadores.get(id).setPalabra(ultimasPalabras.get(language).getLast().toCharArray());

        } catch (ServerNotActiveException e) {
            e.printStackTrace();
        }

        return true;
    }

    // Devuelve si el idioma en el que se quiere jugar esta soportado o no por el
    // servidor
    public boolean languageSupported(String language) throws RemoteException {

        // Carpeta con los diccionarios
        File carpeta = new File("rmiserver/Diccionarios");
        // Diccionarios que tenemos
        File[] archivos = carpeta.listFiles();

        // Si no tenemos diccionarios en la carpeta
        if (archivos == null || archivos.length == 0) {
            System.out.println("No hay idiomas");
            return false;

            // Si tenemos diccionarios en la carpeta
        } else {
            File archivo;
            String fileName;

            // Recorremos los diccionarios
            for (int i = 0; i < archivos.length; i++) {
                archivo = archivos[i];
                fileName = archivo.getName();

                // Comprobando que haya el diccionario que se ha solicitado
                if (fileName.substring(0, fileName.indexOf(".")).equals(language)) {
                    return true;
                }
            }

            return false;
        }
    }

    // Funcion para cargar un diccionario de un archivo en un ArrayList
    public static void leerArchivo(String idioma) {

        // archivo con las palabras de un idioma
        File archivo = new File("rmiserver/Diccionarios/" + idioma + ".txt.midic");

        // si el archivo existe
        if (archivo.exists()) {

            try {

                FileReader fr = new FileReader(archivo); // archivo a leeer
                BufferedReader br = new BufferedReader(fr); // buffer a leer

                // Lectura del fichero
                String linea;
                ArrayList<String> diccionario = new ArrayList<>();
                while ((linea = br.readLine()) != null) {
                    // anado a las palabras de un idioma cada una de las lineas del archivo
                    diccionario.add(linea);
                }

                // meto el nuevo diccionario en el mapa de diccionarios
                diccionarios.put(idioma, diccionario);

                // cierro el archivo
                fr.close();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    // El cliente informa al servidor de que ha finalizado su partida para que lo
    // elimine de la lista de jugadores
    public void finPartida(String hostname) throws RemoteException {
        try {
            jugadores.remove(RemoteServer.getClientHost().concat("@").concat(hostname));
        } catch (ServerNotActiveException e) {
            System.out.println("Error");
        }
    }

}