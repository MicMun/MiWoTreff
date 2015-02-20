/*
 * Copyright 2013-2014 MicMun
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU >General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or >(at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * >without even the implied warranty of MERCHANTABILIT or FITNESS FOR A PARTICULAR PURPOSE.
 * >See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.
 * If not, see >http://www.gnu.org/licenses/.
 */

package de.micmun.android.miwotreff.util;

import java.util.EventListener;

/**
 * Interface for listener of the ProgramLoader.
 *
 * @author Michael Munzert
 * @version 1.0, 13.01.2013
 */
public interface LoaderListener extends EventListener {
   /**
    * Updates ui with count of new or updated entries.
    *
    * @param c count of new or updated entries.
    */
   void update(int c);
}
