package clinica_agendamiento;


public class DatosCombo {
    
    private int codigo;
    private String nombre;
    
    public DatosCombo(int codigo, String nombre) {
        this.codigo = codigo;
        this.nombre = nombre;
    }

    public int getCodigo() {
        return codigo;
    }

    public void setCodigo(int codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    
    public String toString(){
        return this.getNombre();
    }
    
    public int toInt(){
        return this.getCodigo();
    }
    
}
