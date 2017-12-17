package jterm.command;

import jterm.JTerm;
import jterm.io.output.TextColor;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import com.jcraft.jsch.*;

import java.io.InputStream;
import java.util.logging.Level;

public class SSH {
    private static final Logger LOGGER = Logger.getLogger(SSH.class.getName());
    private static JSch jschSSHChannel;
    private static String strUserName;
    private static String strConnectionIP;
    private static int intConnectionPort;
    private static String strPassword;
    private static Session sesConnection;
    private static String errorMessage;

    @Command(name = "ssh", minOptions = 1, syntax = "ssh user@connectionIP")
    public static void makeSSH(List<String> options) {
        String[] arg = options.get(0).split("@");
        strUserName = arg[0];
        strConnectionIP = arg[1];

        try {
            JTerm.out.print(TextColor.INFO, options.get(0) + "'s password: ");
            strPassword = JTerm.userInput.readLine();
            intConnectionPort=22;
            jschSSHChannel = new JSch();
            try {                                                   //TODO
                jschSSHChannel.setKnownHosts("");
            } catch (JSchException jschX) {
                JTerm.out.println(TextColor.INFO,jschX.getMessage());
            }

            String errorMessage = SSH.connect();                    //connection established
            if (errorMessage != null) {
                JTerm.out.println(TextColor.INFO,errorMessage);
            }



        }catch (IOException e){     //handle exception TODO


        }

    }

    public static String connect() {
        String errorMessage = null;

        try {
            sesConnection = jschSSHChannel.getSession(strUserName,
                    strConnectionIP, intConnectionPort);
            sesConnection.setPassword(strPassword);
            sesConnection.setConfig("StrictHostKeyChecking", "no");
            sesConnection.connect(60000);

        } catch (JSchException jschX) {
            errorMessage = jschX.getMessage();
        }

        return errorMessage;
    }

    private static String logWarning(String warnMessage) {
        if (warnMessage != null) {
            LOGGER.log(Level.WARNING, "{0}:{1} - {2}",
                    new Object[]{strConnectionIP, intConnectionPort, warnMessage});
        }

        return warnMessage;
    }

    public static String sendCommand(String command) {
        StringBuilder outputBuffer = new StringBuilder();

        try {
            Channel channel = sesConnection.openChannel("exec");
            ((ChannelExec) channel).setCommand(command);
            InputStream commandOutput = channel.getInputStream();
            channel.connect();
            int readByte = commandOutput.read();

            while (readByte != 0xffffffff) {
                outputBuffer.append((char) readByte);
                readByte = commandOutput.read();
            }
            System.out.println(outputBuffer); //@ssh response
            channel.disconnect();
        } catch (IOException ioX) {
            logWarning(ioX.getMessage());
            return null;
        } catch (JSchException jschX) {
            logWarning(jschX.getMessage());
            return null;
        }

        return outputBuffer.toString();
    }

    public static void close() {
        sesConnection.disconnect();
    }

}