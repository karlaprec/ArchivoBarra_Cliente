package archivoclienteporcentaje;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.DecimalFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ArchivoClientePorcentaje {

    static String direccionip;
    static int nsocket;
    static String ruta;

    public static final String IPv4_REGEX = "^(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\." + "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\." + "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\." + "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";
    public static final Pattern IPv4_PATTERN = Pattern.compile(IPv4_REGEX);

    public static String des = "Documents\\";

    public static void main(String[] args) {
        try {
            direccionip = (args[0]);

        } catch (Exception e) {
            System.out.println("INGRESE IP DE NUEVO");
            System.exit(0);
        }
        try {
            nsocket = Integer.parseInt(args[1]);
        } catch (Exception e) {
            System.out.println("INGRESE SOCKET DE NUEVO");
            System.exit(1);
        }
        try {
            ruta = (args[2]);
        } catch (Exception e) {
            System.out.println("INGRESE RUTA DE NUEVO");
            System.exit(2);
        }

        Socket client = null;
        PrintWriter escritor = null;
        BufferedReader lector = null;
        try {
            client = new Socket(direccionip, nsocket);
        } catch (Exception e) {
            System.err.println("ERROR EN SOCKET");
        }
        try {
            escritor = new PrintWriter(client.getOutputStream(), true);
        } catch (Exception e) {
            System.err.println("ERROR ESCRITOR");
        }
        try {
            lector = new BufferedReader(new InputStreamReader(client.getInputStream()));
        } catch (Exception e) {
            System.err.println("ERROR LECTOR");
        }

        try {
            escritor.println(ruta);
            String datosEntrada = "";
            while (true) {
                try {
                    datosEntrada = lector.readLine();
                    if (datosEntrada.equals("null")) {
                        client.close();
                    }
                } catch (Exception e) {
                    System.exit(2);
                    client.close();
                }
                READ(Long.parseLong(datosEntrada), client);
            }
        } catch (Exception e) {
            System.err.println("ERROR DE COMUNICACION");
        }

    }

    public static void READ(long size, Socket connection) throws IOException {
        BufferedInputStream bis;
        BufferedOutputStream bos;
        byte[] receivedData;
        int in;
        String file;
        receivedData = new byte[1024]; /* TAMAÑO 1024 DEL BUFFER */
        bis = new BufferedInputStream(connection.getInputStream());
        DataInputStream dis = new DataInputStream(connection.getInputStream());
        /* RECIBE */
        file = dis.readUTF();
        file = file.substring(file.indexOf('\\') + 1, file.length());
        /* GUARDA */
        bos = new BufferedOutputStream(new FileOutputStream(des + file));
        System.out.println("TAMAÑO: " + convertirbytes(size));
        long i = 0;
        int repetido = 0;
        int porcentaje = 0;
        while ((in = bis.read(receivedData)) != -1) {
            bos.write(receivedData, 0, in);
            i += in;
            porcentaje = (int) (((double) i / (double) size) * 100);
            if (porcentaje != repetido) {
                repetido = porcentaje;
                barrap(porcentaje, 100, i, size);
            }
        }

        if (porcentaje == 100) {
            System.out.println("DESCARGA FINALIZADA");
        }
        bos.close();
        dis.close();
    }

    public static boolean ipvalida(String ip) {
        if (ip == null) {
            return false;
        }
        Matcher matcher = IPv4_PATTERN.matcher(ip);
        return matcher.matches();
    }

    public static String convertirbytes(long size) {
        String hrSize = null;

        double b = size;
        double kb = size / 1024.0;
        double mb = ((size / 1024.0) / 1024.0);
        double gb = (((size / 1024.0) / 1024.0) / 1024.0);
        double tb = ((((size / 1024.0) / 1024.0) / 1024.0) / 1024.0);

        DecimalFormat dec = new DecimalFormat("0.00");

        if (tb > 1) {
            hrSize = dec.format(tb).concat(" TB");
        } else if (gb > 1) {
            hrSize = dec.format(gb).concat(" GB");
        } else if (mb > 1) {
            hrSize = dec.format(mb).concat(" MB");
        } else if (kb > 1) {
            hrSize = dec.format(kb).concat(" KB");
        } else {
            hrSize = dec.format(b).concat(" Bytes");
        }

        return hrSize;
    }

    public static void barrap(int actual, int total, long tamañoa, long tamañom) {
        if (actual > total) {
            throw new IllegalArgumentException();
        }
        int tamañobarra = 10;
        int porcentaje = ((100 * actual) / total) / tamañobarra;
        char carga = '-';
        String icono = "OOO";
        String barra = new String(new char[tamañobarra]).replace('\0', carga) + "]";
        StringBuilder barraterminada = new StringBuilder();
        barraterminada.append("[");
        for (int i = 0; i < porcentaje; i++) {
            barraterminada.append(icono);
        }
        String bareRemain = barra.substring(porcentaje, barra.length());
        System.out.print("\rDESCARGANDO: " + barraterminada + bareRemain + " " + actual + "%");
        if (actual == total) {
            System.out.print("\n");
        }
    }

}
