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

package edu.cmu.tetrad.util;

import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.*;

/**
 * Sets up streams for logging via the Java logging API. To add a stream to the
 * logging, call addStream(stream, level). The formatter will be a simple
 * formatter that outputs the text of the logging messages only. To remove the
 * stream, call removeStream(stream).
 *
 * @author Joseph Ramsey
 */
public class LogUtils {

    /**
     * Singleton instance.
     */
    private static final LogUtils INSTANCE = new LogUtils();

    /**
     * The logger being used.
     */
    private Logger logger = Logger.getLogger("tetradlog");

    /**
     * Map from streams to handlers.
     */
    private Map<OutputStream, StreamHandler> streams
            = new HashMap<OutputStream, StreamHandler>();

    //============================CONSTRUCTORS===========================//

    /**
     * Private constructor.
     */
    private LogUtils() {
        logger.setUseParentHandlers(false);
        logger.setLevel(Level.FINEST);
    }

    /**
     * Returns singleton instance.
     */
    public static LogUtils getInstance() {
        return INSTANCE;
    }

    //===========================PUBLIC METHODS=========================//

    /**
     * Adds the given stream to logging.
     *
     * @param stream
     * @param level
     */
    public void add(OutputStream stream, Level level) {
        if (stream == null) {
            throw new NullPointerException();
        }

        if (level == null) {
            throw new NullPointerException();
        }

        SimpleFormatter formatter = new SimpleFormatter() {
            @Override
			public synchronized String format(LogRecord record) {
                StringBuffer buf = new StringBuffer();
                buf.append(record.getMessage());
                buf.append("\n");
                return buf.toString();
            }
        };

        StreamHandler handler = new StreamHandler(stream, formatter);
        handler.setLevel(level);
        streams.put(stream, handler);

        getLogger().addHandler(handler);
    }

    /**
     * Sets the logging level for the given stream.
     */
    public void setLevel(OutputStream stream, Level level) {
        Handler handler = streams.get(stream);

        if (handler != null) {
            handler.setLevel(level);
        }
    }

    /**
     * Removes the given stream from logging.
     */
    public void remove(OutputStream stream) {
        if (stream == null) {
            return;
        }

        Handler handler = streams.get(stream);

        if (handler == null) {
            return;
        }

        handler.flush();
        
        if (stream != System.out) {
            handler.close();
        }

        getLogger().removeHandler(handler);
    }

    /**
     * Removes all streams from logging.
     */
    public void clear() {
        for (OutputStream stream : streams.keySet()) {
            remove(stream);
        }
    }

    public void severe(String s) {
        getLogger().severe(s);
    }

    public void warning(String s) {
        getLogger().warning(s);
        flushAll();
    }

    public void config(String s) {
        getLogger().config(s);
        flushAll();
    }

    public void info(String s) {
        getLogger().info(s);
        flushAll();
    }

    public void fine(String s) {
        getLogger().fine(s);
        flushAll();
    }

    public void finer(String s) {
        getLogger().finer(s);
        flushAll();
    }

    public void finest(String s) {
        getLogger().finest(s);
        flushAll();
    }

    private Logger getLogger() {
        return logger;
    }

    private void flushAll() {
        for (OutputStream stream : streams.keySet()) {
            Handler handler = streams.get(stream);
            handler.flush();
        }
    }
}


