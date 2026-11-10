package m3.aplicacion.puerto.entrada;

import m3.dominio.Conductor;

/**
 * Puerto de entrada (Caso de uso - HU-02): Consultar mi perfil como conductor autenticado.
 * Resuelve la identidad del conductor a partir del contexto seguro de M1 (Keycloak).
 */
public interface ObtenerMiConductorPerfilUsoCaso {

    /**
     * Obtiene el perfil y estado actual del conductor autenticado en la sesión o token actual.
     *
     * @return El perfil del conductor autenticado.
     */
    Conductor getMyDriverProfile();
}
