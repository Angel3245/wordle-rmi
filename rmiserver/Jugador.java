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

public class Jugador {

    public String id; // Identificador del jugador
    public String language; // Idioma escogido por el jugador
    public int intentos; // Intentos que lleva el jugador
    public char[] palabra; // Palabra que el jugador debe adivinar

    public Jugador(String id, String language) {
        this.id = id;
        this.language = language;
        this.intentos = 5;
        this.palabra = new char[5];
    }

    public void setPalabra(char[] palabra) {
        this.palabra = palabra;
    }

    public void setIntentos(int intentos) {
        this.intentos = intentos;
    }

}
