///////////////////////////////////////////////////////////////////////////////
// For information as to what this class does, see the Javadoc, below.       //
// Copyright (C) 2005 by Peter Spirtes, Richard Scheines, Joseph Ramsey,     //
// and Clark Glymour.                                                        //
//                                                                           //
// This program is free software; you can redistribute it and/or modify      //
// it under the terms of the GNU General Public License as published by      //
// the Free Software Foundation; either version 2 of the License, or         //
// (at your option) any later version.                                       //
//                                                                           //
// This program is distributed in the hope that it will be useful,           //
// but WITHOUT ANY WARRANTY; without even the implied warranty of            //
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the             //
// GNU General Public License for more details.                              //
//                                                                           //
// You should have received a copy of the GNU General Public License         //
// along with this program; if not, write to the Free Software               //
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA //
///////////////////////////////////////////////////////////////////////////////

package edu.cmu.tetradapp.util;


/**
 * Some utilities for loading license-related files and making sure they get the
 * right versions stamped in them.
 *
 * @author Joseph Ramsey
 * @version $Revision: 4524 $ $Date: 2005-08-30 16:29:17 -0400 (Tue, 30 Aug
 *          2005) $
 */
public final class LicenseUtils {
    public static String copyright() {
        String copyright =
                FileLoadingUtils.fromResources("/resources/copyright");
        String currentVersion = Version.currentViewableVersion().toString();
        copyright = copyright.replaceAll("VERSION", currentVersion);
        return copyright;
    }

    public static String license() {
        return FileLoadingUtils.fromResources("/resources/license");
    }
}
