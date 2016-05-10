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

package edu.cmu;

import edu.cmu.tetradapp.util.TetradSerializableUtils;

/**
 * Performs basic tests to ensure that sessions saved out with previous "stable"
 * versions of Tetrad will load with later "stable" version of Tetrad, where a
 * "stable" version of Tetrad is a version in some sequence V1, V2, ..., Vn of
 * versions, each of which has a saved archive produced by the main() method of
 * this class, and each of which passes the testLoadability() method of this
 * class with respect to the saved archives of each previous version in the
 * list. This test will red-flag any cases in which the name or package package
 * path of a class has been changed with respect to any previous version, or the
 * type of any field has been made incompatible with the type of a field by that
 * same name in a previous version. It will also red-flag any cases in which a
 * field has been removed (or its name changed). It also checks the mundane
 * things things, like making sure serialVerUID is set to a fixed value,
 * which is chosen to be 23L, making sure all TetradSerializable classes (that
 * aren't TetradSerializableExcluded and aren't abstract or interfaces) have
 * serializableInstance() static constructors. If this test passes, then all
 * data from old sessions will be loaded back up, and mistakes of its use will
 * be due to misinterpretation or ignoring of old data by programmers. Adding
 * fields is fine.
 * <p/>
 * The test assumes that there is an identified set of public classes inside the
 * scope "serializableScope" (= "dir") that implement the TetradSerializable
 * interface. It checks to make sure that each serializable field of each of
 * these classes is primitive, of a TetradSerializable type, of a type
 * designated as safe in the safelySerializableTypes array (see), or is an array
 * of one of these types.
 * <p/>
 * The test is as follows. First, the "current" directory is cleared, and then
 * the serializableInstance() method for each TetradSerializable class C in dir
 * is called to produce a serializable instance I of C. I is then serialized out
 * to the "current" directory. At the same time, the list of field names for
 * each class is compiled, and this list is saved out to the "current"
 * directory.
 * <p/>
 * Next, serialized files in the "current" directory are deserialized, just make
 * sure this is possible. For default serialization (which we're using) this is
 * not necessary, but if the default serialization method is overridden, it's
 * necessary to make sure fields are read in in the order they are written out,
 * so the test is included.
 * <p/>
 * Finally, each serialized file in the archive saved out to the "archive"
 * directory is deserialized, just make sure this is possible. If anyone has
 * moved or renamed a class, or changed the type of a field to an incompatible
 * type (where any change of primitive type is incompatible), which won't be
 * possible, and an exception will be thrown, with information about which
 * archive the change conflict with. At the same time, the list of fields for
 * each class in each archive is read back in, and the classes in the current
 * directory are checked to make sure they have fields by those names. If
 * current classes have fields by the same names as previous versions of those
 * classes, then the types of those fields  must be  compatible, since the
 * deserialization test for each archive must also pass. This guarantees that
 * fields cannot be dropped or names of fields changed.
 * <p/>
 * The main() method of this class (called by Ant) performs one additional step,
 * when a new version of Tetrad is being posted online--namely, it zips up the
 * "current" directory (produced in the first step) and saves this zip file (an
 * archive) to the "archives" directory. The point of this is to make sure
 * conflicts are only found with previous published versions and not with all
 * previous versions. (Developmental versions are counted as published
 * versions.)
 * <p/>
 * It is assumed that each instantiable TetradSerializable class C  in dir has a
 * static constructor of the following form:
 * <pre>
 * public static C* serializableInstance() {
 *     // Returns an instance of C, of type C* = C or some super class C*
 *     // of C (if this method is overriding a method of C*).
 * }
 * </pre>
 * The instance returned may be mind-numbingly simple and may change over time.
 * The point is to make sure that instances serialized out with earlier versions
 * load with the currentDirectory version, and this is primarily a matter of
 * ensuring that the type of each field in the earlier version is compatible
 * with the field in the later version of the same name, if a field in the later
 * version by that name exists. It is not necessary to consider different
 * instantiations or subclasses of the type of a field, since all
 * TetradSerializable classes are already tested.
 * <p/>
 * For more information on binary serialization, see the Serialization spec:
 * <pre>
 * http://java.sun.com/j2se/1.4.2/docs/guide/serialization/
 * </pre>
 * Or this useful blurb:
 * <pre>
 * http://java.sun.com/developer/technicalArticles/Programming/serialization/
 * </pre>
 * Or Joshua Block, Effective Java.
 *
 * @author Joseph Ramsey
 * @version $Revision: 5308 $ $Date: 2005-08-30 16:29:17 -0400 (Tue, 30 Aug
 *          2005) $
 * @see edu.cmu.tetradapp.util.TetradSerializableUtils
 */
public class UpdateVersion {
    public UpdateVersion() {
    }

    public static void main(String[] args) {
        String serializableScope = "build/tetrad/classes/edu/cmu";
        String currentDirectory = "build/tetrad/serializable/current";
        String archiveDirectory = "archives";

        TetradSerializableUtils utils = new TetradSerializableUtils(
                serializableScope, currentDirectory, archiveDirectory);

        utils.updateVersion();
    }
}


