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

    // Opcion 2: Gram-Schmidt para tres vectores.
    static void gramSchmidt() {
        System.out.println("\nMETODO DE GRAM-SCHMIDT");

        int n = pedirEntero("Ingrese la dimension de los vectores: ");

        if (n < 3) {
            System.out.println("La dimension debe ser al menos 3.");
            return;
        }

        Fraccion[] v1 = pedirVector(n, 1);
        Fraccion[] v2 = pedirVector(n, 2);
        Fraccion[] v3 = pedirVector(n, 3);

        Fraccion[] u1 = v1.clone();
        Fraccion p11 = productoPunto(u1, u1);

        if (p11.esCero()) {
            System.out.println("El primer vector no puede ser cero.");
            return;
        }

        Fraccion coef21 = productoPunto(v2, u1).dividir(p11);
        Fraccion[] u2 = new Fraccion[n];

        for (int i = 0; i < n; i++) {
            u2[i] = v2[i].restar(coef21.multiplicar(u1[i]));
        }

        Fraccion p22 = productoPunto(u2, u2);

        if (p22.esCero()) {
            System.out.println("Los dos primeros vectores son dependientes.");
            return;
        }

        Fraccion coef31 = productoPunto(v3, u1).dividir(p11);
        Fraccion coef32 = productoPunto(v3, u2).dividir(p22);
        Fraccion[] u3 = new Fraccion[n];

        for (int i = 0; i < n; i++) {
            u3[i] = v3[i]
                .restar(coef31.multiplicar(u1[i]))
                .restar(coef32.multiplicar(u2[i]));
        }

        System.out.println("\nRESULTADOS:");
        mostrarVector("u1", u1);
        mostrarVector("u2", u2);
        mostrarVector("u3", u3);

        Fraccion p12 = productoPunto(u1, u2);
        Fraccion p13 = productoPunto(u1, u3);
        Fraccion p23 = productoPunto(u2, u3);

        System.out.println("\nCOMPROBACION:");
        System.out.println("u1 . u2 = " + p12);
        System.out.println("u1 . u3 = " + p13);
        System.out.println("u2 . u3 = " + p23);

        if (productoPunto(u3, u3).esCero()) {
            System.out.println(
                "El tercer vector es dependiente de los anteriores."
            );
        } else if (p12.esCero() && p13.esCero() && p23.esCero()) {
            System.out.println("Los tres vectores son ortogonales.");
        } else {
            System.out.println("La comprobacion no se cumple.");
        }
    }

    // Opcion 3: los cuatro ejercicios de transformaciones.
    static void transformaciones() {
        int[][][] matrices = {
            {{1, 2}, {2, -1}, {1, 3}},
            {{2, -1}, {1, 3}, {4, -1}},
            {{3, 1}, {1, -2}, {2, 4}},
            {{1, -3}, {2, 2}, {5, -1}}
        };

        String[] formulas = {
            "T(x,y) = (x + 2y, 2x - y, x + 3y)",
            "T(x,y) = (2x - y, x + 3y, 4x - y)",
            "T(x,y) = (3x + y, x - 2y, 2x + 4y)",
            "T(x,y) = (x - 3y, 2x + 2y, 5x - y)"
        };

        for (int p = 0; p < matrices.length; p++) {
            int[][] m = matrices[p];

            System.out.println("\nPROBLEMA " + (p + 1));
            System.out.println(formulas[p]);
            System.out.println(
                "T(1,0) = (" + m[0][0] + ", " + m[1][0]
                + ", " + m[2][0] + ")"
            );
            System.out.println(
                "T(0,1) = (" + m[0][1] + ", " + m[1][1]
                + ", " + m[2][1] + ")"
            );

            System.out.println("Matriz resultante:");

            for (int[] fila : m) {
                System.out.println("[ " + fila[0] + "  " + fila[1] + " ]");
            }
        }
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