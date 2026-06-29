/*******************************************************************************
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package org.vash.vate.net.sourceforge.jsocks.test;

import java.net.Socket;

import org.vash.vate.net.sourceforge.jsocks.socks.*;
import org.vash.vate.net.sourceforge.jsocks.socks.server.*;

/** Test file for UserPasswordAuthentictor */

public class UPSOCKS implements UserValidation{
    String user, password;

    UPSOCKS(String user,String password){
       this.user = user;
       this.password = password;
    }

    public boolean isUserValid(String user,String password,Socket s){
       System.err.println("User:"+user+"\tPassword:"+password);
       System.err.println("Socket:"+s);
       return (user.equals(this.user) && password.equals(this.password));
    }

    public static void main(String args[]){
        String user, password;

        if(args.length == 2){
          user = args[0];
          password = args[1];
        }else{
          user = "user";
          password = "password";
        }

        UPSOCKS us = new UPSOCKS(user,password);
        UserPasswordAuthenticator auth = new UserPasswordAuthenticator(us);
        ProxyServer server = new ProxyServer(auth);

        server.start(1080);
    }

    public String getUsername()
    {
      return user;
    }

    public String getPassword()
    {
      return password;
    }

    public String[] getUsernames() {
      return null;
    }

    public String[] getPasswords() {
      return null;
    }
}
