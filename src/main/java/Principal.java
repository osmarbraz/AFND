/**
 *
 * @author osmar
 */
public class Principal {

    public static void main(String[] args) {
        System.out.println("Automato Finito Não Determinístico(AFND)");
        
        AFND afnd = new AFND("0");
        afnd.avaliar();
    }
}
