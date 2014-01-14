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

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;

import java.net.Socket;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Player class.
 *
 * @author Kay Anar
 * @version 1.0
 *
 */
public class Player
  extends Thread
{
  private static final double VERSION = 1.0;
  private static final String VT100_INIT = "\u001b[0m";
  private static final String VT100_HOME = "\u001b[H";
  private static final String VT100_CLEAR = "\u001b[J";
  private static Logger _logger = Logger.getLogger("com.anars.telnetmation");

  private Socket _socket;
  private DataOutputStream _dataOutputStream;
  private BufferedReader _bufferedReader;
  private boolean _center;

  Player(Socket socket, File file, boolean center)
    throws Exception
  {
    super();
    _socket = socket;
    _center = center;
    try
    {
      _dataOutputStream = new DataOutputStream(socket.getOutputStream());
      _bufferedReader = new BufferedReader(new FileReader(file));
      _logger.log(Level.FINE, "Connection from " + socket.getRemoteSocketAddress().toString());
    }
    catch (Exception exception)
    {
      closeAll();
      throw new Exception(exception);
    }
  }

  public void run()
  {
    try
    {
      int width = 0;
      int height = 0;
      long delayMultiplier = 0;
      int delay = -1;
      int leftMargin = 0;
      int topMargin = 0;
      String line;
      int lineCount = 0;
      String output = "";
      _dataOutputStream.writeBytes(VT100_INIT + VT100_CLEAR + VT100_HOME);
      while ((line = _bufferedReader.readLine()) != null)
      {
        if (delayMultiplier == 0)
        {
          String[] info = line.split(":");
          if (Double.parseDouble(info[0]) > VERSION)
            System.out.println("");
          width = Integer.parseInt(info[1]);
          height = Integer.parseInt(info[2]);
          if (_center)
          {
            leftMargin = (80 - width) / 2;
            topMargin = (24 - height) / 2;
          }
          delayMultiplier = Long.parseLong(info[3]);
        }
        else
        {
          if (delay == -1)
            delay = Integer.parseInt(line);
          else
          {
            line = line.replaceAll("\\s+$", "");
            for (int index = 0; !line.equals("") && _center && index < leftMargin; index++)
              output += " ";
            output += line;
            if (++lineCount < height)
              output += "\n";
            else
            {
              _dataOutputStream.writeBytes(VT100_HOME + VT100_CLEAR);
              for (int index = 0; _center && index < topMargin; index++)
                _dataOutputStream.writeBytes("\n");
              _dataOutputStream.writeBytes(output + VT100_HOME);
              _dataOutputStream.flush();
              sleep(delayMultiplier * delay);
              output = "";
              delay = -1;
              lineCount = 0;
            }
          }
        }
      }
      _dataOutputStream.writeBytes(VT100_CLEAR + VT100_HOME);
    }
    catch (Throwable throwable)
    {
      _logger.log(Level.WARNING, "", throwable);
    }
    finally
    {
      closeAll();
    }
  }

  private void closeAll()
  {
    if (_bufferedReader != null)
      try
      {
        _bufferedReader.close();
      }
      catch (Exception exception)
      {
        _logger.log(Level.WARNING, "BufferedReader close", exception);
      }
      finally
      {
        _bufferedReader = null;
      }
    if (_dataOutputStream != null)
      try
      {
        _dataOutputStream.close();
      }
      catch (Exception exception)
      {
        _logger.log(Level.WARNING, "DataOutputStream close", exception);
      }
      finally
      {
        _dataOutputStream = null;
      }
    if (_socket != null)
      try
      {
        _socket.close();
      }
      catch (Exception exception)
      {
        _logger.log(Level.WARNING, "Socket close", exception);
      }
      finally
      {
        _socket = null;
      }
  }
}
