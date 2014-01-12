/**
 * Telnetmation - Telnet Based ASCII Animation Player Server
 * Copyright (c) 2014 Anar Software LLC. < http://anars.com >
 * 
 * This program is free software: you can redistribute it and/or modify it under 
 * the terms of the GNU General Public License as published by the Free Software 
 * Foundation, either version 3 of the License, or (at your option) any later 
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS 
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with 
 * this program.  If not, see < http://www.gnu.org/licenses/ >
 * 
 */
package com.anars.telnetmation;

import java.io.File;

import java.net.InetAddress;
import java.net.ServerSocket;

import java.util.Calendar;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main class.
 *
 * @author Kay Anar
 * @version 1.0
 *
 */
public class Main
{
  private static Logger _logger = Logger.getLogger("com.anars.telnetmation");

  public Main(String[] args)
  {
    int listenPort = 0;
    InetAddress bindAddress = null;
    File filePath = null;
    boolean center = false;
    Level loggerLevel = Level.ALL;
    for (int index = 0; index < args.length; index++)
    {
      String values[] = args[index].split("=");
      values[0] = values[0].trim().toLowerCase();
      if (values[0].equals("-port"))
      {
        try
        {
          listenPort = Integer.parseInt(values[1]);
          if (listenPort < 0 || listenPort > 65535)
            errorExit("-port value must be between 1 and 65535", -1);
        }
        catch (Exception exception)
        {
          _logger.log(Level.SEVERE, "-port value", exception);
          errorExit("Invalid -port value", -2);
        }
      }
      else if (values[0].equals("-address"))
      {
        try
        {
          bindAddress = InetAddress.getByName(values[1]);
        }
        catch (Exception exception)
        {
          _logger.log(Level.SEVERE, "-address value", exception);
          errorExit("Invalid -address value", -3);
        }
      }
      else if (values[0].equals("-file"))
      {
        try
        {
          filePath = new File(values[1]);
          if (!filePath.exists())
            errorExit("Invalid -file value, \"" + filePath.toString() + "\" not exists", -4);
        }
        catch (Exception exception)
        {
          _logger.log(Level.SEVERE, "-file value", exception);
          errorExit("Invalid -file value", -5);
        }
      }
      else if (values[0].equals("-log"))
      {
        try
        {
          loggerLevel = Level.parse(values[1].trim().toUpperCase());
        }
        catch (Exception exception)
        {
          _logger.log(Level.SEVERE, "-log value", exception);
          errorExit("Invalid -log value", -6);
        }

      }
      else if (values[0].equals("-center"))
      {
        try
        {
          values[1] = values[1].trim().toLowerCase();
          if (values[1].equals("true"))
            center = true;
          else if (values[1].equals("false"))
            center = false;
          else
          {
            _logger.log(Level.SEVERE, "-center value", values[1]);
            errorExit("Invalid -center value", -7);
          }
        }
        catch (Exception exception)
        {
          _logger.log(Level.SEVERE, "-center value", exception);
          errorExit("Invalid -center value", -5);
        }

      }
      else if (values[0].equals("-help"))
      {
        System.out.println("\nTelnetmation version 1.0\n" + //
          "Copyright (c) " + Calendar.getInstance().get(Calendar.YEAR) + " Anar Software LLC. < http://anars.com >\n\n" + //
          "This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.\n\n" + //
          "This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.\n" + //
          "See the GNU General Public License for more details at http://www.gnu.org/licenses\n\n" + //
          "USAGE:\n" + //
          "\tjava -jar telnetmation.jar [PARAMETERS]...\n\n" + //
          "MANDATORY PARAMETER:\n\n" + //
          "-file=[FILE-PATH]\n" + //
          "\tFull path to ascii animation file.\n" + //
          "\tE.g. -file=ascii_animation.txt\n\n" + //
          "OPTIONAL PARAMETERS:\n\n" + //
          "-port=[NUMBER]\n" + //
          "\tThe TCP port on which telnetmation will listen. The number must be between 1 and 65535, inclusive. Default, any free local TCP port.\n" + //
          "\tE.g. -port=8080\n\n" + //
          "-address=[INET-ADDRESS]\n" + //
          "\tLocal IP address or hostname which telnetmation will only accept connection requests from. Default, any/all local addresses.\n" + //
          "\tE.g. -address=192.168.1.100\n\n" + //
          "-log=[LEVEL]\n" + //
          "\tSet the log level specifying which message levels will be logged/displayed\n" + //
          "\tOptions are ALL,CONFIG,FINE,FINER,FINEST,INFO,OFF,SEVERE, and WARNING. Default is ALL.\n" + //
          "\tE.g. -log=INFO\n\n" + //
          "-center=[TRUE-FALSE]\n" + //
          "\tCenters ASCII animation both horizontally and vertically on the terminal screen.\n" + //
          "\tE.g. -center=true\n");
        System.exit(0);
      }
      else
      {
        _logger.log(Level.SEVERE, "Unknown parameter \"" + args[index] + "\"");
        errorExit("Unknown parameter \"" + args[index] + "\"", -8);
      }
    }

    _logger.setLevel(loggerLevel);

    Handler[] handlers = Logger.getLogger("").getHandlers();
    for (int index = 0; index < handlers.length; index++)
      handlers[index].setLevel(loggerLevel);

    handlers = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).getHandlers();
    for (int index = 0; index < handlers.length; index++)
      handlers[index].setLevel(loggerLevel);

    if (filePath == null)
      errorExit("-file parameter is missing", -8);

    ServerSocket serverSocket = null;
    try
    {
      serverSocket = new ServerSocket(listenPort, 0, bindAddress);
    }
    catch (Exception exception)
    {
      errorExit("Unable to bind socket - " + exception.getMessage(), -9);
    }
    _logger.log(Level.INFO, "Telnetmation started.");
    _logger.log(Level.CONFIG, "Host  : " + serverSocket.getInetAddress().getHostAddress());
    _logger.log(Level.CONFIG, "Port  : " + serverSocket.getLocalPort());
    _logger.log(Level.CONFIG, "File  : " + filePath);
    _logger.log(Level.CONFIG, "Center: " + center);
    while (true)
    {
      try
      {
        (new Player(serverSocket.accept(), filePath, center)).start();
      }
      catch (Exception exception)
      {
        _logger.log(Level.SEVERE, "", exception);
      }
    }
  }

  public static void main(String[] args)
  {
    new Main(args);
  }

  private void errorExit(String message, int errorCode)
  {
    System.err.println(message + ". Please type -help for details.");
    System.exit(errorCode);
  }
}
