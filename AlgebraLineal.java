import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Scanner;

public class AlgebraLineal {

    static Scanner entrada = new Scanner(System.in);

    // Fracciones exactas, sin redondeos.
    static class Fraccion {
        BigInteger n;
        BigInteger d;

        Fraccion(long valor) {
            this(BigInteger.valueOf(valor), BigInteger.ONE);
        }

        Fraccion(BigInteger numerador, BigInteger denominador) {
            if (denominador.signum() == 0) {
                throw new ArithmeticException("Denominador igual a cero.");
            }

            if (denominador.signum() < 0) {
                numerador = numerador.negate();
                denominador = denominador.negate();
            }

            BigInteger mcd = numerador.gcd(denominador);
            n = numerador.divide(mcd);
            d = denominador.divide(mcd);
        }

        static Fraccion leer(String texto) {
            texto = texto.trim();

            if (texto.contains("/")) {
                String[] partes = texto.split("/", -1);

                if (partes.length != 2) {
                    throw new NumberFormatException();
                }

                return new Fraccion(
                    new BigInteger(partes[0].trim()),
                    new BigInteger(partes[1].trim())
                );
            }

            BigDecimal decimal = new BigDecimal(texto);
            BigInteger numerador = decimal.unscaledValue();
            int escala = decimal.scale();

            if (escala < 0) {
                return new Fraccion(
                    numerador.multiply(BigInteger.TEN.pow(-escala)),
                    BigInteger.ONE
                );
            }

            return new Fraccion(
                numerador,
                BigInteger.TEN.pow(escala)
            );
        }

        Fraccion sumar(Fraccion otra) {
            return new Fraccion(
                n.multiply(otra.d).add(otra.n.multiply(d)),
                d.multiply(otra.d)
            );
        }

        Fraccion restar(Fraccion otra) {
            return new Fraccion(
                n.multiply(otra.d).subtract(otra.n.multiply(d)),
                d.multiply(otra.d)
            );
        }

        Fraccion multiplicar(Fraccion otra) {
            return new Fraccion(
                n.multiply(otra.n),
                d.multiply(otra.d)
            );
        }

        Fraccion dividir(Fraccion otra) {
            return new Fraccion(
                n.multiply(otra.d),
                d.multiply(otra.n)
            );
        }

        boolean esCero() {
            return n.signum() == 0;
        }

        public String toString() {
            if (d.equals(BigInteger.ONE)) {
                return n.toString();
            }
            return n + "/" + d;
        }
    }

    static Fraccion pedirNumero(String mensaje) {
        while (true) {
            System.out.print(mensaje);

            try {
                return Fraccion.leer(entrada.nextLine());
            } catch (NumberFormatException | ArithmeticException e) {
                System.out.println(
                    "Dato incorrecto. Ejemplos: 5, -2.5 o 3/4."
                );
            }
        }
    }

    static int pedirEntero(String mensaje) {
        while (true) {
            System.out.print(mensaje);

            try {
                return Integer.parseInt(entrada.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Ingrese un numero entero valido.");
            }
        }
    }

    static void mostrarMatriz(String titulo, Fraccion[][] matriz) {
        System.out.println("\n" + titulo);

        for (Fraccion[] fila : matriz) {
            System.out.print("[ ");

            for (Fraccion valor : fila) {
                System.out.printf("%10s ", valor);
            }

            System.out.println("]");
        }
    }

    // Opcion 1: factorizacion LU por Doolittle.
    static void factorizacionLU() {
        Fraccion[][] a = new Fraccion[3][3];
        Fraccion[][] l = new Fraccion[3][3];
        Fraccion[][] u = new Fraccion[3][3];

        System.out.println("\nFACTORIZACION LU DE UNA MATRIZ 3x3");
        System.out.println("Ingrese los 9 elementos de la matriz.");

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                a[i][j] = pedirNumero(
                    "Ingrese a" + (i + 1) + (j + 1) + ": "
                );
                u[i][j] = a[i][j];
                l[i][j] = new Fraccion(i == j ? 1 : 0);
            }
        }

        mostrarMatriz("MATRIZ ORIGINAL A:", a);

        for (int pivote = 0; pivote < 2; pivote++) {
            if (u[pivote][pivote].esCero()) {
                System.out.println("\nEl pivote es cero.");
                System.out.println(
                    "Se necesita pivoteo; esta opcion no lo realiza."
                );
                return;
            }

            for (int fila = pivote + 1; fila < 3; fila++) {
                Fraccion multiplicador =
                    u[fila][pivote].dividir(u[pivote][pivote]);

                l[fila][pivote] = multiplicador;

                System.out.println(
                    "\nF" + (fila + 1) + " = F" + (fila + 1)
                    + " - (" + multiplicador + ") * F" + (pivote + 1)
                );

                for (int columna = 0; columna < 3; columna++) {
                    Fraccion anterior = u[fila][columna];
                    Fraccion referencia = u[pivote][columna];

                    u[fila][columna] = anterior.restar(
                        multiplicador.multiplicar(referencia)
                    );

                    System.out.println(
                        "Columna " + (columna + 1) + ": "
                        + anterior + " - (" + multiplicador + ") * ("
                        + referencia + ") = " + u[fila][columna]
                    );
                }

                mostrarMatriz("Matriz despues de la operacion:", u);
            }
        }

        mostrarMatriz("MATRIZ L:", l);
        mostrarMatriz("MATRIZ U:", u);

        Fraccion[][] resultado = new Fraccion[3][3];
        boolean coincide = true;

        System.out.println("\nCOMPROBACION L * U = A");

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                Fraccion suma = new Fraccion(0);
                System.out.print("c" + (i + 1) + (j + 1) + " = ");

                for (int k = 0; k < 3; k++) {
                    if (k > 0) {
                        System.out.print(" + ");
                    }

                    System.out.print(
                        "(" + l[i][k] + ") * (" + u[k][j] + ")"
                    );

                    suma = suma.sumar(l[i][k].multiplicar(u[k][j]));
                }

                resultado[i][j] = suma;
                System.out.println(" = " + suma);

                if (!suma.restar(a[i][j]).esCero()) {
                    coincide = false;
                }
            }
        }

        mostrarMatriz("RESULTADO DE L * U:", resultado);

        if (coincide) {
            System.out.println("COMPROBACION CORRECTA: L * U = A.");
        } else {
            System.out.println("L * U no coincide con A.");
        }
    }

    static Fraccion productoPunto(Fraccion[] a, Fraccion[] b) {
        Fraccion suma = new Fraccion(0);

        for (int i = 0; i < a.length; i++) {
            suma = suma.sumar(a[i].multiplicar(b[i]));
        }

        return suma;
    }

    static Fraccion[] pedirVector(int n, int numero) {
        Fraccion[] vector = new Fraccion[n];

        System.out.println("\nIngrese el vector " + numero);

        for (int i = 0; i < n; i++) {
            vector[i] = pedirNumero("Componente " + (i + 1) + ": ");
        }

        return vector;
    }

    static void mostrarVector(String nombre, Fraccion[] vector) {
        System.out.print(nombre + " = (");

        for (int i = 0; i < vector.length; i++) {
            if (i > 0) {
                System.out.print(", ");
            }
            System.out.print(vector[i]);
        }

        System.out.println(")");
    }

    static Fraccion productoPuntoConPasos(
        String nombre, Fraccion[] a, Fraccion[] b
    ) {
        System.out.print(nombre + " = ");
        Fraccion suma = new Fraccion(0);

        for (int i = 0; i < a.length; i++) {
            if (i > 0) {
                System.out.print(" + ");
            }

            System.out.print("(" + a[i] + ") * (" + b[i] + ")");
            suma = suma.sumar(a[i].multiplicar(b[i]));
        }

        System.out.println(" = " + suma);
        return suma;
    }

    static Fraccion[] proyeccionConPasos(
        String nombre,
        Fraccion coeficiente,
        String nombreBase,
        Fraccion[] base
    ) {
        System.out.println(
            nombre + " = (" + coeficiente + ") * " + nombreBase
        );

        Fraccion[] proyeccion = new Fraccion[base.length];

        for (int i = 0; i < base.length; i++) {
            proyeccion[i] = coeficiente.multiplicar(base[i]);

            System.out.println(
                "Componente " + (i + 1) + ": (" + coeficiente
                + ") * (" + base[i] + ") = " + proyeccion[i]
            );
        }

        mostrarVector(nombre, proyeccion);
        return proyeccion;
    }

    // Opcion 2: Gram-Schmidt con procedimiento y fracciones exactas.
    static void gramSchmidt() {
        System.out.println("\nMETODO DE GRAM-SCHMIDT PASO A PASO");

        int n = pedirEntero("Ingrese la dimension de los vectores: ");

        if (n < 3) {
            System.out.println("La dimension debe ser al menos 3.");
            return;
        }

        Fraccion[] v1 = pedirVector(n, 1);
        Fraccion[] v2 = pedirVector(n, 2);
        Fraccion[] v3 = pedirVector(n, 3);

        System.out.println("\nVECTORES INGRESADOS:");
        mostrarVector("v1", v1);
        mostrarVector("v2", v2);
        mostrarVector("v3", v3);

        System.out.println("\nPASO 1: CALCULAR u1");
        System.out.println("u1 = v1");

        Fraccion[] u1 = v1.clone();
        mostrarVector("u1", u1);

        Fraccion p11 = productoPuntoConPasos("u1 . u1", u1, u1);

        if (p11.esCero()) {
            System.out.println(
                "El primer vector es cero. No se puede dividir entre u1 . u1 = 0."
            );
            System.out.println(
                "Ingrese un primer vector distinto de cero para usar este procedimiento."
            );
            return;
        }

        System.out.println("\nPASO 2: CALCULAR u2");
        System.out.println(
            "u2 = v2 - ((v2 . u1) / (u1 . u1)) * u1"
        );

        Fraccion p21 = productoPuntoConPasos("v2 . u1", v2, u1);
        Fraccion coef21 = p21.dividir(p11);

        System.out.println(
            "Coeficiente 21 = (" + p21 + ") / (" + p11 + ") = " + coef21
        );

        Fraccion[] proy21 = proyeccionConPasos(
            "proy21", coef21, "u1", u1
        );

        System.out.println("u2 = v2 - proy21");

        Fraccion[] u2 = new Fraccion[n];

        for (int i = 0; i < n; i++) {
            u2[i] = v2[i].restar(proy21[i]);

            System.out.println(
                "u2[" + (i + 1) + "] = (" + v2[i] + ") - ("
                + proy21[i] + ") = " + u2[i]
            );
        }

        mostrarVector("u2", u2);

        Fraccion p22 = productoPuntoConPasos("u2 . u2", u2, u2);

        if (p22.esCero()) {
            System.out.println(
                "Los dos primeros vectores son dependientes: u2 es cero."
            );
            System.out.println(
                "No se puede usar u2 como divisor para calcular u3 con esta formula."
            );
            System.out.println(
                "Elija dos primeros vectores linealmente independientes."
            );
            return;
        }

        System.out.println("\nPASO 3: CALCULAR u3");
        System.out.println(
            "u3 = v3 - ((v3 . u1) / (u1 . u1)) * u1"
            + " - ((v3 . u2) / (u2 . u2)) * u2"
        );

        Fraccion p31 = productoPuntoConPasos("v3 . u1", v3, u1);
        Fraccion coef31 = p31.dividir(p11);

        System.out.println(
            "Coeficiente 31 = (" + p31 + ") / (" + p11 + ") = " + coef31
        );

        Fraccion[] proy31 = proyeccionConPasos(
            "proy31", coef31, "u1", u1
        );

        Fraccion p32 = productoPuntoConPasos("v3 . u2", v3, u2);
        Fraccion coef32 = p32.dividir(p22);

        System.out.println(
            "Coeficiente 32 = (" + p32 + ") / (" + p22 + ") = " + coef32
        );

        Fraccion[] proy32 = proyeccionConPasos(
            "proy32", coef32, "u2", u2
        );

        System.out.println("u3 = v3 - proy31 - proy32");

        Fraccion[] u3 = new Fraccion[n];

        for (int i = 0; i < n; i++) {
            Fraccion parcial = v3[i].restar(proy31[i]);
            u3[i] = parcial.restar(proy32[i]);

            System.out.println(
                "u3[" + (i + 1) + "] = (" + v3[i] + ") - ("
                + proy31[i] + ") - (" + proy32[i] + ")"
            );

            System.out.println(
                "       = (" + parcial + ") - (" + proy32[i]
                + ") = " + u3[i]
            );
        }

        mostrarVector("u3", u3);

        System.out.println("\nRESULTADOS FINALES (SIN NORMALIZAR):");
        mostrarVector("u1", u1);
        mostrarVector("u2", u2);
        mostrarVector("u3", u3);

        System.out.println("\nPASO 4: COMPROBAR ORTOGONALIDAD");

        Fraccion p12 = productoPuntoConPasos("u1 . u2", u1, u2);
        Fraccion p13 = productoPuntoConPasos("u1 . u3", u1, u3);
        Fraccion p23 = productoPuntoConPasos("u2 . u3", u2, u3);

        if (productoPunto(u3, u3).esCero()) {
            System.out.println(
                "El tercer vector es dependiente de los anteriores: u3 = 0."
            );
            System.out.println(
                "Aunque los productos punto dan cero, solo se obtuvieron"
                + " dos vectores no nulos ortogonales."
            );
        } else if (p12.esCero() && p13.esCero() && p23.esCero()) {
            System.out.println(
                "COMPROBACION CORRECTA: los tres productos punto son cero."
            );
            System.out.println(
                "Se obtuvieron tres vectores no nulos, ortogonales"
                + " y linealmente independientes."
            );
        } else {
            System.out.println(
                "La comprobacion de ortogonalidad no se cumple."
            );
        }
    }

    // Opcion 3: transformacion ingresada por el usuario.
    static void transformaciones() {
        System.out.println("\nTRANSFORMACIONES LINEALES PASO A PASO");
        int columnas;
        do {
            columnas = pedirEntero("Cantidad de columnas (2 a 5): ");
            if (columnas < 2 || columnas > 5) {
                System.out.println("Ingrese una cantidad entre 2 y 5.");
            }
        } while (columnas < 2 || columnas > 5);

        String[] variables = {"x", "y", "z", "w", "v"};
        Fraccion[][] matriz = new Fraccion[3][columnas];
        System.out.println("Ingrese enteros, negativos, decimales o fracciones.");
        for (int i = 0; i < 3; i++) {
            System.out.println("\nComponente " + (i + 1) + " de T:");
            for (int j = 0; j < columnas; j++) {
                matriz[i][j] = pedirNumero("Coeficiente de " + variables[j] + ": ");
            }
        }
        String argumentos = "";
        for (int j = 0; j < columnas; j++) {
            if (j > 0) argumentos += ", ";
            argumentos += variables[j];
        }
        System.out.println("\nTRANSFORMACION INGRESADA:");
        System.out.print("T(" + argumentos + ") = (");
        for (int i = 0; i < 3; i++) {
            if (i > 0) System.out.print(", ");
            for (int j = 0; j < columnas; j++) {
                if (j > 0) System.out.print(" + ");
                System.out.print("(" + matriz[i][j] + ")" + variables[j]);
            }
        }
        System.out.println(")");

        String[] bases = new String[columnas];
        for (int columna = 0; columna < columnas; columna++) {
            String base = "(";
            for (int j = 0; j < columnas; j++) {
                if (j > 0) base += ",";
                base += (j == columna ? "1" : "0");
            }
            base += ")";
            bases[columna] = base;
            System.out.println("\nPASO " + (columna + 1) + ": CALCULAR T" + base);
            Fraccion[] imagen = new Fraccion[3];
            for (int i = 0; i < 3; i++) {
                Fraccion suma = new Fraccion(0);
                String operacion = "";
                for (int j = 0; j < columnas; j++) {
                    int valor = (j == columna ? 1 : 0);
                    suma = suma.sumar(matriz[i][j].multiplicar(new Fraccion(valor)));
                    if (j > 0) operacion += " + ";
                    operacion += "(" + matriz[i][j] + ")(" + valor + ")";
                }
                imagen[i] = suma;
                System.out.println("Componente " + (i + 1) + ": " + operacion + " = " + suma);
            }
            mostrarVector("T" + base, imagen);
        }
        System.out.println("\nPASO " + (columnas + 1) + ": CONSTRUIR LA MATRIZ");
        for (int j = 0; j < columnas; j++) {
            System.out.println("Columna " + (j + 1) + ": T" + bases[j]);
        }
        mostrarMatriz("Matriz de la transformacion:", matriz);
    }

    public static void main(String[] args) {
        int opcion;

        do {
            System.out.println("\n================================");
            System.out.println("      MENU DE ALGEBRA LINEAL");
            System.out.println("================================");
            System.out.println("1. Factorizacion LU");
            System.out.println("2. Gram-Schmidt");
            System.out.println("3. Transformaciones lineales");
            System.out.println("4. Salir");

            opcion = pedirEntero("Seleccione una opcion: ");

            switch (opcion) {
                case 1:
                    factorizacionLU();
                    break;
                case 2:
                    gramSchmidt();
                    break;
                case 3:
                    transformaciones();
                    break;
                case 4:
                    System.out.println("Programa finalizado.");
                    break;
                default:
                    System.out.println("Opcion no valida.");
            }
        } while (opcion != 4);

        entrada.close();
    }
}
