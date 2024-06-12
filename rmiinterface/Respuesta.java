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

package rmiinterface;

import java.io.Serializable;

public class Respuesta implements Serializable // para el informe
{
    private static final long serialVersionUID = 20120731125400L; //UID para asegurar que el objeto que le llega al cliente es el mismo que el que espera

    public boolean error; //indica si se ha producido un error
    public String color; //contiene letras que consisten en los colores asociados a la palabra enviada ('v':verde, 'g':gris, 'a':amarillo)
    public String msg; //contiene un mensaje informativo o de error

    public Respuesta() {
        this.error = false;
        this.color = null;
        this.msg = null;
    }

}
