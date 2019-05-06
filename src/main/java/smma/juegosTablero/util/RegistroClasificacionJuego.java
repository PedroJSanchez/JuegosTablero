/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package smma.juegosTablero.util;

import java.util.Objects;
import juegosTablero.dominio.elementos.ClasificacionJuego;

/**
 *
 * @author pedroj
 */
public class RegistroClasificacionJuego {
    private final String idJuego;
    private final ClasificacionJuego clasificacion;

    public RegistroClasificacionJuego(String idJuego, ClasificacionJuego clasificacion) {
        this.idJuego = idJuego;
        this.clasificacion = clasificacion;
    }

    
    public String getIdJuego() {
        return idJuego;
    }

    public ClasificacionJuego getClasificacion() {
        return clasificacion;
    }

    @Override
    public String toString() {
        return "RegistroJuego{" + "idJuego=" + idJuego + ", clasificacion=" + clasificacion + '}';
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 67 * hash + Objects.hashCode(this.idJuego);
        hash = 67 * hash + Objects.hashCode(this.clasificacion);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final RegistroClasificacionJuego other = (RegistroClasificacionJuego) obj;
        if (!Objects.equals(this.idJuego, other.idJuego)) {
            return false;
        }
        return true;
    }
}
