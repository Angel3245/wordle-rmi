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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

public class Hilo implements Runnable {

    String language; // idioma asignado al hilo para cambiar palabras

    public Hilo(String language) {
        this.language = language;
    }

    public void run() {

        String palabraEscogida;
        ArrayList<String> diccionario;
        LinkedList<String> ultimasPalabrasIdioma;

        ArrayList<String> comunesSpanish = leerComunesSpanish();

        while (hayJugadoresIdioma()) {
            try {

                // Cogemos las palabras de mi idioma
                if (language.equals("spanish")) {
                    diccionario = comunesSpanish;
                } else {
                    diccionario = Server.diccionarios.get(language);
                }

                // Cogemos las ultimas palabras que han salido de mi idioma
                ultimasPalabrasIdioma = Server.ultimasPalabras.get(language);

                do {
                    // Escoger una palabra al azar de las de mi idioma
                    palabraEscogida = diccionario.get((int) Math.floor(Math.random() * (diccionario.size())));

                    // Si la palabra fue una de las cinco ultimas en salir escojo otra
                } while (ultimasPalabrasIdioma.contains(palabraEscogida));

                // Si la lista de ultimas palabras jugadas ya tiene cinco
                if (ultimasPalabrasIdioma.size() >= 5) {
                    // Quito la que mas tiempo lleva
                    Server.ultimasPalabras.get(language).poll();
                }

                // Meto en la lista la nueva palabra escogida
                Server.ultimasPalabras.get(language).addLast(palabraEscogida);

                System.out.println("Para el idioma " + language + " se ha escogido: " + palabraEscogida);

                // Espera de 5 minutos
                Thread.sleep(300000);

            } catch (InterruptedException exc) {
                System.out.println("ERROR");
            }
        }

        Server.diccionarios.remove(this.language); //eliminamos el diccionario asociado al lenguaje
        Server.ultimasPalabras.remove(this.language); //eliminamos las ultimas palabras escogidas asociadas al lenguaje

        System.out.println("Los jugadores con idioma " + this.language + " han acabado sus partidas");

    }

    public ArrayList<String> leerComunesSpanish() {
        // archivo con las palabras mas comunes del espanol
        File archivo = new File("rmiserver/Diccionarios/spanish.2.txt.midic");
        ArrayList<String> diccionario = new ArrayList<>();

        try {
            FileReader fr = new FileReader(archivo); // archivo a leeer
            BufferedReader br = new BufferedReader(fr); // buffer a leer
            // Lectura del fichero
            String linea;
            while ((linea = br.readLine()) != null) {
                // anado a las palabras de un idioma cada una de las lineas del archivo
                diccionario.add(linea);
            }
            // cierro el archivo
            fr.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return diccionario;

    }

    public boolean hayJugadoresIdioma() {
        Collection<Jugador> jugadores = Server.jugadores.values();

        for (Jugador jugador : jugadores) {
            if (jugador.language.equals(this.language)) {
                return true;
            }
        }
        return false;
    }

}
