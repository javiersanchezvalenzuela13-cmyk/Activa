package org.example.activa.model;

public class MetodoPago {

    private int idMetodoPago;
    private int idUsuario;
    private String tipo;
    private String titular;
    private String identificador;
    private String fechaCaducidad;
    private boolean esPredeterminado;

    public MetodoPago() {
    }

    public MetodoPago(int idUsuario, String tipo, String titular, String identificador, String fechaCaducidad, boolean esPredeterminado) {
        this.idUsuario = idUsuario;
        this.tipo = tipo;
        this.titular = titular;
        this.identificador = identificador;
        this.fechaCaducidad = fechaCaducidad;
        this.esPredeterminado = esPredeterminado;
    }

    public int getIdMetodoPago() { return idMetodoPago; }
    public void setIdMetodoPago(int idMetodoPago) { this.idMetodoPago = idMetodoPago; }

    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getTitular() { return titular; }
    public void setTitular(String titular) { this.titular = titular; }

    public String getIdentificador() { return identificador; }
    public void setIdentificador(String identificador) { this.identificador = identificador; }

    public String getFechaCaducidad() { return fechaCaducidad; }
    public void setFechaCaducidad(String fechaCaducidad) { this.fechaCaducidad = fechaCaducidad; }

    public boolean isEsPredeterminado() { return esPredeterminado; }
    public void setEsPredeterminado(boolean esPredeterminado) { this.esPredeterminado = esPredeterminado; }
}