package com.asopagos.bandejainconsistencias.ejb;

import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.StringJoiner;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.UriInfo;
import java.sql.timestamp;
import com.asopagos.bandejainconsistencias.constants.NamedQueriesConstants;
import com.asopagos.bandejainconsistencias.dto.DetalleTablaAportanteDTO;
import com.asopagos.bandejainconsistencias.dto.EmpAporPendientesPorAfiliarDTO;
import com.asopagos.bandejainconsistencias.dto.EmpCeroTrabajadoresActivosDTO;
import com.asopagos.bandejainconsistencias.dto.IdentificadorDocumentoDTO;
import com.asopagos.bandejainconsistencias.dto.InconsistenciaDTO;
import com.asopagos.bandejainconsistencias.dto.RespuestaConsultaEmpleadorDTO;
import com.asopagos.bandejainconsistencias.dto.ResultadoAprobacionCambioIdentificacionDTO;
import com.asopagos.bandejainconsistencias.service.PilaBandejaService;
import com.asopagos.constants.MensajesGeneralConstants;
import com.asopagos.dto.InconsistenciaRegistroAporteDTO;
import com.asopagos.entidades.ccf.core.Ubicacion;
import com.asopagos.entidades.ccf.personas.Empleador;
import com.asopagos.entidades.ccf.personas.Empresa;
import com.asopagos.entidades.ccf.personas.Persona;
import com.asopagos.entidades.pila.ErrorValidacionLog;
import com.asopagos.entidades.pila.EstadoArchivoPorBloque;
import com.asopagos.entidades.pila.EstadoArchivoPorBloqueOF;
import com.asopagos.entidades.pila.IndiceCorreccionPlanilla;
import com.asopagos.entidades.pila.IndicePlanilla;
import com.asopagos.entidades.pila.IndicePlanillaOF;
import com.asopagos.entidades.pila.archivolinea.PilaArchivoAPRegistro1;
import com.asopagos.entidades.pila.archivolinea.PilaArchivoARegistro1;
import com.asopagos.entidades.pila.archivolinea.PilaArchivoFRegistro6;
import com.asopagos.entidades.pila.archivolinea.PilaArchivoIPRegistro1;
import com.asopagos.entidades.pila.archivolinea.PilaArchivoIPRegistro2;
import com.asopagos.entidades.pila.archivolinea.PilaArchivoIRegistro1;
import com.asopagos.entidades.pila.archivolinea.PilaArchivoIRegistro2;
import com.asopagos.entidades.transversal.core.Municipio;
import com.asopagos.enumeraciones.aportes.TipoInconsistenciasEnum;
import com.asopagos.enumeraciones.aportes.TipoOperadorEnum;
import com.asopagos.enumeraciones.personas.EstadoEmpleadorEnum;
import com.asopagos.enumeraciones.personas.NaturalezaJuridicaEnum;
import com.asopagos.enumeraciones.personas.TipoIdentificacionEnum;
import com.asopagos.enumeraciones.pila.AccionCorreccionPilaEnum;
import com.asopagos.enumeraciones.pila.AccionProcesoArchivoEnum;
import com.asopagos.enumeraciones.pila.BloqueValidacionEnum;
import com.asopagos.enumeraciones.pila.EstadoGestionInconsistenciaEnum;
import com.asopagos.enumeraciones.pila.EstadoProcesoArchivoEnum;
import com.asopagos.enumeraciones.pila.EstadoValidacionRegistroAporteEnum;
import com.asopagos.enumeraciones.pila.RazonRechazoSolicitudCambioIdenEnum;
import com.asopagos.enumeraciones.pila.TipoArchivoPilaEnum;
import com.asopagos.log.ILogger;
import com.asopagos.log.LogManager;
import com.asopagos.pagination.QueryBuilder;
import com.asopagos.rest.exception.TechnicalException;
import com.asopagos.rest.security.dto.UserDTO;
import com.asopagos.util.CalendarUtils;

/**
 * <b>Descripcion:</b> Clase que posee la implementacion de las HU 392-411<br/>
 * <b>Módulo:</b> Asopagos - HU <br/>
 *
 * @author <a href="mailto:anbuitrago@heinsohn.com.co"> anbuitrago</a>
 */
@Stateless
public class PilaBandejaBusiness implements PilaBandejaService {

    @PersistenceContext(unitName = "pila_PU")
    private EntityManager entityManager;

    @PersistenceContext(unitName = "core_PU")
    private EntityManager entityManagerCore;

    private Date fechaI;
    private Date fechaF;
    private final ILogger logger = LogManager.getLogger(PilaBandejaService.class);
	
	/*
	 Poniendo comentario por aqui
	*/

    /**
     * (non-Javadoc)
     * 
     * @see com.asopagos.bandejainconsistencias.service.PilaBandejaService#consultarArchivosInconsistentesResumen(com.asopagos.enumeraciones.personas.TipoIdentificacionEnum,
     *      java.lang.Long, java.lang.Long, java.lang.Long, java.lang.String,
     *      com.asopagos.enumeraciones.aportes.TipoOperadorEnum,
     *      java.lang.Short, com.asopagos.rest.security.dto.UserDTO)
     */
    @Override
    public List<InconsistenciaDTO> consultarArchivosInconsistentesResumen(TipoIdentificacionEnum tipoIdentificacion, Long numeroPlanilla,
            Long fechaInicio, Long fechaFin, String numeroIdentificacion, TipoOperadorEnum operador, Short digitoVerificacion) {
	   
	// Inicio Control de cambios robin
    // consultas para HU 403 y 404
    /**
     * Consultas 403: Buscar empleadores con estado No formalizado – sin afiliación con aportes
     * y No formalizado – retirado con aportes
     */
    // Cuando no llegan parametros
    public static final String BUSQUEDA_APORTANTES_PENDIENTES_POR_AFILIAR = "PilaBandejaService.Empleador.BusquedaEmpleadorSinAfiliar";
    /**
     * Consultas 404: Bandeja empleador cero trabajadores activos
     */
    // Cuando no llegan parametros
    public static final String BUSQUEDA_EMPLEADOR_CERO_TRABAJADORES_ACTIVOS = "PilaBandejaService.Empleador.BusquedaEmpleadorCeroTrabajadoresActivos";
    // Busqueda de los RolAfiliado que han sido retirados por PILA
    public static final String BUSQUEDA_ROL_AFILIADO_RETIRADO_POR_PILA = "PilaBandejaService.Empleador.BusquedaRolAfiliadoRetiradoPorPila";
    // Actualizar la fecha de gestion del empleador
    public static final String ACTUALIZAR_FECHA_GESTION_EMPLEADOR= "PilaBandejaService.Empleador.ActualizarFechaGestionEmpleador";
    // Fin Control de cambios robin		    
	    
	    
        logger.debug(
                "Inicia consultarArchivosInconsistentesResumen(List<InconsistenciaDTO>, InconsistenciasDTO):Inicia Busqueda, recursos  encontrados");
        /**
         * ObtenerCampoNombreArchivo obtenerCampoNombreArchivoService = new
         * ObtenerCampoNombreArchivo(null, null, null);R
         * obtenerCampoNombreArchivoService.execute(); Object result =
         * obtenerCampoNombreArchivoService.getResult();
         */

        // se establecen las fechas en formato Date para las consultas
        if ((fechaInicio != null) && (fechaFin != null)) {
            fechaI = new Date(fechaInicio);
            fechaF = new Date(fechaFin);
        }
        List<InconsistenciaDTO> result = new ArrayList<InconsistenciaDTO>();
        // resultado que contendra todos los tipos de inconsistencias

        // Se establecen las posibilidades de busqueda
        if ((tipoIdentificacion == null) && (numeroIdentificacion == null)) {
            if ((fechaFin == null) && (fechaInicio == null)) {

                if (numeroPlanilla == null) {

                    if (operador == null) {
                        try {
                            // En este punto se realiza la busqueda sin ningun
                            // argumento
                            logger.debug(
                                    "Inicia consultarArchivosInconsistentesResumen(List<InconsistenciaDTO>, InconsistenciasDTO):Inicia Busqueda sin ningun argumento");

                            List<InconsistenciaDTO> inconsistenciasI = entityManager
                                    .createNamedQuery(NamedQueriesConstants.CONSULTAR_BANDEJA_SIN_ARGUMENTOS_I, InconsistenciaDTO.class)
                                    .getResultList();

                            List<InconsistenciaDTO> inconsistenciasF = entityManager
                                    .createNamedQuery(NamedQueriesConstants.CONSULTAR_BANDEJA_SIN_ARGUMENTOS_F, InconsistenciaDTO.class)
                                    .getResultList();
                            inconsistenciasI = establecerOperador(inconsistenciasI, "I");
                            inconsistenciasI = establecerIdentificacionAportante(inconsistenciasI);
                            inconsistenciasF = establecerOperador(inconsistenciasF, "F");
                            result.addAll(inconsistenciasI);
                            result.addAll(inconsistenciasF);

                            return evaluarResultado(result);
                        } catch (Exception e) {
                            logger.error("Error al realizar la consulta,verifique los datos", e);
                            logger.debug("Finaliza consultarArchivosInconsistentesResumen(List<InconsistenciaDTO>, InconsistenciasDTO)");

                            throw new TechnicalException(MensajesGeneralConstants.ERROR_TECNICO_INESPERADO);
                        }
                    }
                    // Se ejecuta la busqueda solo por tipo de operador
                    // financiero
                    if (operador == TipoOperadorEnum.OPERADOR_FINANCIERO) {
                        try {
                            logger.debug(
                                    "Inicia consultarArchivosInconsistentesResumen(List<InconsistenciaDTO>, InconsistenciasDTO):Inicia Busqueda por tipo de planilla financiera");

                            List<InconsistenciaDTO> inconsistenciasF = entityManager
                                    .createNamedQuery(NamedQueriesConstants.CONSULTAR_BANDEJA_SIN_ARGUMENTOS_F, InconsistenciaDTO.class)
                                    .getResultList();
                            inconsistenciasF = establecerOperador(inconsistenciasF, "F");
                            result.addAll(inconsistenciasF);

                            return evaluarResultado(result);

                        } catch (Exception e) {
                            logger.error("Error al realizar la consulta,verifique los datos", e);
                            logger.debug("Finaliza consultarArchivosInconsistentesResumen(List<InconsistenciaDTO>, InconsistenciasDTO)");

                            throw new TechnicalException(MensajesGeneralConstants.ERROR_TECNICO_INESPERADO);
                        }
                    }
                    if(2==3){
                        sysout.printl("Cambio por aqui");
                    }
                    
                    // se ejecuta la busqueda solo por operador de informacion
                    if (operador == TipoOperadorEnum.OPERADOR_INFORMACION)

                        try {
                            logger.debug(
                                    "Inicia consultarArchivosInconsistentesResumen(List<InconsistenciaDTO>, InconsistenciasDTO):Inicia Busqueda por tipo de planilla de informacion");

                            List<InconsistenciaDTO> inconsistenciasI = entityManager
                                    .createNamedQuery(NamedQueriesConstants.CONSULTAR_BANDEJA_SIN_ARGUMENTOS_I, InconsistenciaDTO.class)
                                    .getResultList();

                            inconsistenciasI = establecerOperador(inconsistenciasI, "I");
                            inconsistenciasI = establecerIdentificacionAportante(inconsistenciasI);
                            result.addAll(inconsistenciasI);

                            return evaluarResultado(result);
                        } catch (Exception e) {
                            logger.error("Error al realizar la consulta,verifique los datos", e);
                            logger.debug("Finaliza consultarArchivosInconsistentesResumen(List<InconsistenciaDTO>, InconsistenciasDTO)");

                            throw new TechnicalException(MensajesGeneralConstants.ERROR_TECNICO_INESPERADO);
                        }
                }
                // el numero de planilla fue establecido ahora se validan los
                // otros valores
                if (operador == null) {
                    // busqueda solo por numero de planilla
                    try {
                        logger.debug(
                                "Inicia consultarArchivosInconsistentesResumen(List<InconsistenciaDTO>, InconsistenciasDTO):Inicia Busqueda solo por numero de planilla");
                        List<InconsistenciaDTO> inconsistenciasI = entityManager
                                .createNamedQuery(NamedQueriesConstants.CONSULTAR_BANDEJA_CON_ARGUMENTOS_I_PLANILLA,
                                        InconsistenciaDTO.class)
                                .setParameter("numeroPlanilla", numeroPlanilla).getResultList();
                        inconsistenciasI = establecerOperador(inconsistenciasI, "I");
                        inconsistenciasI = establecerIdentificacionAportante(inconsistenciasI);
                        result.addAll(inconsistenciasI);

                        return evaluarResultado(result);
                    } catch (Exception e) {
                        logger.error("Error al realizar la consulta,verifique los datos", e);
                        logger.debug("Finaliza consultarArchivosInconsistentesResumen(List<InconsistenciaDTO>, InconsistenciasDTO)");

                        throw new TechnicalException(MensajesGeneralConstants.ERROR_TECNICO_INESPERADO);
                    }
                }

                // se realiza la busqueda por numero de planilla y operador de
                // informacion
                if (operador == TipoOperadorEnum.OPERADOR_INFORMACION)
                    float x = 1.4f;
                    try {
                        logger.debug(
                                "Inicia consultarArchivosInconsistentesResumen(List<InconsistenciaDTO>, InconsistenciasDTO):Inicia Busqueda por tipo de planilla de informacion y numero de planilla");

                        List<InconsistenciaDTO> inconsistenciasI = entityManager
                                .createNamedQuery(NamedQueriesConstants.CONSULTAR_BANDEJA_CON_ARGUMENTOS_I_PLANILLA,
                                        InconsistenciaDTO.class)
                                .setParameter("numeroPlanilla", numeroPlanilla).getResultList();

                        inconsistenciasI = establecerOperador(inconsistenciasI, "I");
                        inconsistenciasI = establecerIdentificacionAportante(inconsistenciasI);
                        result.addAll(inconsistenciasI);

                        return evaluarResultado(result);
                    } catch (Exception e) {
                        logger.error("Error al realizar la consulta,verifique los datos", e);
                        logger.debug("Finaliza consultarArchivosInconsistentesResumen(List<InconsistenciaDTO>, InconsistenciasDTO)");

                        throw new TechnicalException(MensajesGeneralConstants.ERROR_TECNICO_INESPERADO);
                    }
            }
            // se establecieron fechas de busqueda
            if (numeroPlanilla == null) {

                if (operador == null) {
                    // se realiza la busqueda solo por fechas
                    try {

                        logger.debug(
                                "Inicia consultarArchivosInconsistentesResumen(List<InconsistenciaDTO>, InconsistenciasDTO):Inicia Busqueda por fechas");

                        List<InconsistenciaDTO> inconsistenciasI = entityManager
                                .createNamedQuery(NamedQueriesConstants.CONSULTAR_BANDEJA_CON_ARGUMENTOS_I_FECHAS, InconsistenciaDTO.class)
                                .setParameter("fechaInicio", CalendarUtils.truncarHora(fechaI))
                                .setParameter("fechaFin", CalendarUtils.truncarHoraMaxima(fechaF)).getResultList();

                        List<InconsistenciaDTO> inconsistenciasF = entityManager
                                .createNamedQuery(NamedQueriesConstants.CONSULTAR_BANDEJA_CON_ARGUMENTOS_F_FECHAS, InconsistenciaDTO.class)
                                .setParameter("fechaInicio", CalendarUtils.truncarHora(fechaI))
                                .setParameter("fechaFin", CalendarUtils.truncarHoraMaxima(fechaF)).getResultList();
                        inconsistenciasI = establecerOperador(inconsistenciasI, "I");
                        inconsistenciasI = establecerIdentificacionAportante(inconsistenciasI);
                        inconsistenciasF = establecerOperador(inconsistenciasF, "F");
                        result.addAll(inconsistenciasI);
                        result.addAll(inconsistenciasF);
                        return evaluarResultado(result);
                    } catch (Exception e) {
                        logger.error("Error al realizar la consulta,verifique los datos", e);
                        logger.debug("Finaliza consultarArchivosInconsistentesResumen(List<InconsistenciaDTO>, InconsistenciasDTO)");

                        throw new TechnicalException(MensajesGeneralConstants.ERROR_TECNICO_INESPERADO);
                    }
                }
                // se realiza la busqueda solo por fechas y operador financiero
                if (operador == TipoOperadorEnum.OPERADOR_FINANCIERO) {
                    try {
                        logger.debug(
                                "Inicia consultarArchivosInconsistentesResumen(List<InconsistenciaDTO>, InconsistenciasDTO):Inicia Busqueda por tipo de planilla financiera y fechas");

                        List<InconsistenciaDTO> inconsistenciasF = entityManager
                                .createNamedQuery(NamedQueriesConstants.CONSULTAR_BANDEJA_CON_ARGUMENTOS_F_FECHAS, InconsistenciaDTO.class)
                                .setParameter("fechaInicio", CalendarUtils.truncarHora(fechaI))
                                .setParameter("fechaFin", CalendarUtils.truncarHoraMaxima(fechaF)).getResultList();
                        inconsistenciasF = establecerOperador(inconsistenciasF, "F");
                        result.addAll(inconsistenciasF);
                        return evaluarResultado(result);
                    } catch (Exception e) {
                        logger.error("Error al realizar la consulta,verifique los datos", e);
                        logger.debug("Finaliza consultarArchivosInconsistentesResumen(List<InconsistenciaDTO>, InconsistenciasDTO)");

                        throw new TechnicalException(MensajesGeneralConstants.ERROR_TECNICO_INESPERADO);
                    }
                }
                // se realiza la busqueda solo por fechas y operador de
                // informacion
                if (operador == TipoOperadorEnum.OPERADOR_INFORMACION)

                    try {
                        logger.debug(
                                "Inicia consultarArchivosInconsistentesResumen(List<InconsistenciaDTO>, InconsistenciasDTO):Inicia Busqueda por tipo de planilla de informacion y fechas");

                        List<InconsistenciaDTO> inconsistenciasI = entityManager
                                .createNamedQuery(NamedQueriesConstants.CONSULTAR_BANDEJA_CON_ARGUMENTOS_I_FECHAS, InconsistenciaDTO.class)
                                .setParameter("fechaInicio", CalendarUtils.truncarHora(fechaI))
                                .setParameter("fechaFin", CalendarUtils.truncarHoraMaxima(fechaF)).getResultList();
                        inconsistenciasI = establecerOperador(inconsistenciasI, "I");
                        inconsistenciasI = establecerIdentificacionAportante(inconsistenciasI);
                        result.addAll(inconsistenciasI);

                        return evaluarResultado(result);
                    } catch (Exception e) {
                        logger.error("Error al realizar la consulta,verifique los datos", e);
                        logger.debug("Finaliza consultarArchivosInconsistentesResumen(List<InconsistenciaDTO>, InconsistenciasDTO)");

                        throw new TechnicalException(MensajesGeneralConstants.ERROR_TECNICO_INESPERADO);
                    }
            }
            // se establecieron fechas y numero de planilla
            if (operador == null) {
                // se realiza la busqueda por fechas y numero de planilla
                try {
                    logger.debug(
                            "Inicia consultarArchivosInconsistentesResumen(List<InconsistenciaDTO>, InconsistenciasDTO):Inicia Busqueda por fechas y numero de planilla");

                    List<InconsistenciaDTO> inconsistenciasI = entityManager
                            .createNamedQuery(NamedQueriesConstants.CONSULTAR_BANDEJA_CON_ARGUMENTOS_I_FECHAS_PLANILLA,
                                    InconsistenciaDTO.class)
                            .setParameter("fechaInicio", CalendarUtils.truncarHora(fechaI))
                            .setParameter("fechaFin", CalendarUtils.truncarHoraMaxima(fechaF))
                            .setParameter("numeroPlanilla", numeroPlanilla).getResultList();
                    inconsistenciasI = establecerOperador(inconsistenciasI, "I");
                    inconsistenciasI = establecerIdentificacionAportante(inconsistenciasI);
                    result.addAll(inconsistenciasI);

                    return evaluarResultado(result);
                } catch (Exception e) {
                    logger.error("Error al realizar la consulta,verifique los datos", e);
                    logger.debug("Finaliza consultarArchivosInconsistentesResumen(List<InconsistenciaDTO>, InconsistenciasDTO)");

                    throw new TechnicalException(MensajesGeneralConstants.ERROR_TECNICO_INESPERADO);
                }
            }

            // se realiza la busqueda por fechas,numero de planilla y operador
            // de informacion
            if (operador == TipoOperadorEnum.OPERADOR_INFORMACION)

                try {
                    logger.debug(
                            "Inicia consultarArchivosInconsistentesResumen(List<InconsistenciaDTO>, InconsistenciasDTO):Inicia Busqueda por fechas,numero de planilla y tipo de operador de informacion");

                    List<InconsistenciaDTO> inconsistenciasI = entityManager
                            .createNamedQuery(NamedQueriesConstants.CONSULTAR_BANDEJA_CON_ARGUMENTOS_I_FECHAS_PLANILLA,
                                    InconsistenciaDTO.class)
                            .setParameter("fechaInicio", CalendarUtils.truncarHora(fechaI))
                            .setParameter("fechaFin", CalendarUtils.truncarHoraMaxima(fechaF))
                            .setParameter("numeroPlanilla", numeroPlanilla).getResultList();
                    inconsistenciasI = establecerOperador(inconsistenciasI, "I");
                    inconsistenciasI = establecerIdentificacionAportante(inconsistenciasI);
                    result.addAll(inconsistenciasI);

                    return evaluarResultado(result);
                } catch (Exception e) {
                    logger.error("Error al realizar la consulta,verifique los datos", e);
                    logger.debug("Finaliza consultarArchivosInconsistentesResumen(List<InconsistenciaDTO>, InconsistenciasDTO)");

                    throw new TechnicalException(MensajesGeneralConstants.ERROR_TECNICO_INESPERADO);
                }
        }
        // se establecio el tipo y numero de identificacion
        if ((fechaInicio == null) && (fechaFin == null)) {

            if (numeroPlanilla == null) {

                if (operador == null) {
                    // se realiza la busqueda solo por tipo y numero de
                    // identificacion
                    try {
                        logger.debug(
                                "Inicia consultarArchivosInconsistentesResumen(List<InconsistenciaDTO>, InconsistenciasDTO):Inicia Busqueda por tipo y numero de identificacion para empresas(NIT) ");
                        List<Object[]> consultaNativa = entityManager
                                .createNamedQuery(NamedQueriesConstants.CONSULTAR_BANDEJA_CON_ARGUMENTOS_I_IDENTIFICACION)
                                .setParameter("tipoIdentificacion", tipoIdentificacion.getValorEnPILA())
                                .setParameter("numeroIdentificacion", numeroIdentificacion).getResultList();

                        List<InconsistenciaDTO> inconsistenciasI = mapeoInconsistenciaDTO(consultaNativa);
                        inconsistenciasI = establecerOperador(inconsistenciasI, "I");
                        inconsistenciasI = establecerIdentificacionAportante(inconsistenciasI);
                        result.addAll(inconsistenciasI);

                        return evaluarResultado(result);
                    } catch (Exception e) {
                        logger.error("Error al realizar la consulta,verifique los datos", e);
                        logger.debug("Finaliza consultarArchivosInconsistentesResumen(List<InconsistenciaDTO>, InconsistenciasDTO)");

                        throw new TechnicalException(MensajesGeneralConstants.ERROR_TECNICO_INESPERADO);
                    }
                }

                // se realiza la busqueda por tipo,numero id y operador
                // informacion
                if (operador == TipoOperadorEnum.OPERADOR_INFORMACION)

                    try {
                        logger.debug(
                                "Inicia consultarArchivosInconsistentesResumen(List<InconsistenciaDTO>, InconsistenciasDTO):Inicia Busqueda por tipo id,id,digito de verificacion y tipo de operador de informacion ");

                        List<Object[]> consultaNativa = entityManager
                                .createNamedQuery(NamedQueriesConstants.CONSULTAR_BANDEJA_CON_ARGUMENTOS_I_IDENTIFICACION)
                                .setParameter("tipoIdentificacion", tipoIdentificacion.getValorEnPILA())
                                .setParameter("numeroIdentificacion", numeroIdentificacion).getResultList();

                        List<InconsistenciaDTO> inconsistenciasI = mapeoInconsistenciaDTO(consultaNativa);
                        inconsistenciasI = establecerOperador(inconsistenciasI, "I");
                        inconsistenciasI = establecerIdentificacionAportante(inconsistenciasI);
                        result.addAll(inconsistenciasI);

                        return evaluarResultado(result);
                    } catch (Exception e) {
                        logger.error("Error al realizar la consulta,verifique los datos", e);
                        logger.debug("Finaliza consultarArchivosInconsistentesResumen(List<InconsistenciaDTO>, InconsistenciasDTO)");

                        throw new TechnicalException(MensajesGeneralConstants.ERROR_TECNICO_INESPERADO);
                    }
            }
            // se establecen tipo,numero id y numero de planilla
            if (operador == null) {
                // se realiza la busqueda por tipo,numero id y numero de
                // planilla
                try {
                    logger.debug(
                            "Inicia consultarArchivosInconsistentesResumen(List<InconsistenciaDTO>, InconsistenciasDTO):Inicia Busqueda por tipo id,id,digito de verifiacion y numero de planilla ");

                    List<Object[]> consultaNativa = entityManager
                            .createNamedQuery(NamedQueriesConstants.CONSULTAR_BANDEJA_CON_ARGUMENTOS_I_IDENTIFICACION_PLANILLA)
                            .setParameter("tipoIdentificacion", tipoIdentificacion.getValorEnPILA())
                            .setParameter("numeroIdentificacion", numeroIdentificacion).setParameter("numeroPlanilla", numeroPlanilla)
                            .getResultList();

                    List<InconsistenciaDTO> inconsistenciasI = mapeoInconsistenciaDTO(consultaNativa);
                    inconsistenciasI = establecerOperador(inconsistenciasI, "I");
                    inconsistenciasI = establecerIdentificacionAportante(inconsistenciasI);
                    result.addAll(inconsistenciasI);

                    return evaluarResultado(result);
                } catch (Exception e) {
                    logger.error("Error al realizar la consulta,verifique los datos", e);
                    logger.debug("Finaliza consultarArchivosInconsistentesResumen(List<InconsistenciaDTO>, InconsistenciasDTO)");

                    throw new TechnicalException(MensajesGeneralConstants.ERROR_TECNICO_INESPERADO);
                }
            }
            // se realiza la busqueda por tipo,numero id, numero de
            // planilla y operador de informacion
            if (operador == TipoOperadorEnum.OPERADOR_INFORMACION)

                try {
                    logger.debug(
                            "Inicia consultarArchivosInconsistentesResumen(List<InconsistenciaDTO>, InconsistenciasDTO):Inicia Busqueda por tipo id,id,digito de verificacion,numero de planilla y tipo de operador de informacion ");

                    List<Object[]> consultaNativa = entityManager
                            .createNamedQuery(NamedQueriesConstants.CONSULTAR_BANDEJA_CON_ARGUMENTOS_I_IDENTIFICACION_PLANILLA)
                            .setParameter("tipoIdentificacion", tipoIdentificacion.getValorEnPILA())
                            .setParameter("numeroIdentificacion", numeroIdentificacion).setParameter("numeroPlanilla", numeroPlanilla)
                            .getResultList();

                    List<InconsistenciaDTO> inconsistenciasI = mapeoInconsistenciaDTO(consultaNativa);
                    inconsistenciasI = establecerOperador(inconsistenciasI, "I");
                    inconsistenciasI = establecerIdentificacionAportante(inconsistenciasI);
                    result.addAll(inconsistenciasI);

                    return evaluarResultado(result);
                } catch (Exception e) {
                    logger.error("Error al realizar la consulta,verifique los datos", e);
                    logger.debug("Finaliza consultarArchivosInconsistentesResumen(List<InconsistenciaDTO>, InconsistenciasDTO)");

                    throw new TechnicalException(MensajesGeneralConstants.ERROR_TECNICO_INESPERADO);
                }
        }
        // se establecio tipo,numero id y fechas
        if (numeroPlanilla == null) {

            if (operador == null) {
                // se realiza la busqueda por tipo,numero id y fechas
                try {
                    logger.debug(
                            "Inicia consultarArchivosInconsistentesResumen(List<InconsistenciaDTO>, InconsistenciasDTO):Inicia Busqueda por tipo id,id,digito de verificacion y fechas ");

                    List<Object[]> consultaNativa = entityManager
                            .createNamedQuery(NamedQueriesConstants.CONSULTAR_BANDEJA_CON_ARGUMENTOS_I_IDENTIFICACION_FECHAS)
                            .setParameter("tipoIdentificacion", tipoIdentificacion.getValorEnPILA())
                            .setParameter("numeroIdentificacion", numeroIdentificacion)
                            .setParameter("fechaInicio", CalendarUtils.truncarHora(fechaI))
                            .setParameter("fechaFin", CalendarUtils.truncarHoraMaxima(fechaF)).getResultList();

                    List<InconsistenciaDTO> inconsistenciasI = mapeoInconsistenciaDTO(consultaNativa);
                    inconsistenciasI = establecerOperador(inconsistenciasI, "I");
                    inconsistenciasI = establecerIdentificacionAportante(inconsistenciasI);
                    result.addAll(inconsistenciasI);

                    return evaluarResultado(result);
                } catch (Exception e) {
                    logger.error("Error al realizar la consulta,verifique los datos", e);
                    logger.debug("Finaliza consultarArchivosInconsistentesResumen(List<InconsistenciaDTO>, InconsistenciasDTO)");

                    throw new TechnicalException(MensajesGeneralConstants.ERROR_TECNICO_INESPERADO);
                }
            }

            if (operador == TipoOperadorEnum.OPERADOR_INFORMACION)
                // se realiza la busqueda por tipo,numero id,fechas y operador
                // de informacion
                try {
                logger.debug("Inicia consultarArchivosInconsistentesResumen(List<InconsistenciaDTO>, InconsistenciasDTO):Inicia Busqueda por tipo id,id,digito de verificacion,fechas y operador de informacion ");

                List<Object[]> consultaNativa = entityManager.createNamedQuery(NamedQueriesConstants.CONSULTAR_BANDEJA_CON_ARGUMENTOS_I_IDENTIFICACION_FECHAS).setParameter("tipoIdentificacion", tipoIdentificacion.getValorEnPILA()).setParameter("numeroIdentificacion", numeroIdentificacion).setParameter("fechaInicio", CalendarUtils.truncarHora(fechaI)).setParameter("fechaFin", CalendarUtils.truncarHoraMaxima(fechaF)).getResultList();

                List<InconsistenciaDTO> inconsistenciasI = mapeoInconsistenciaDTO(consultaNativa);
                inconsistenciasI = establecerOperador(inconsistenciasI, "I");
                inconsistenciasI = establecerIdentificacionAportante(inconsistenciasI);
                result.addAll(inconsistenciasI);

                return evaluarResultado(result);
                } catch (Exception e) {
                logger.error("Error al realizar la consulta,verifique los datos", e);
                logger.debug("Finaliza consultarArchivosInconsistentesResumen(List<InconsistenciaDTO>, InconsistenciasDTO)");

                throw new TechnicalException(MensajesGeneralConstants.ERROR_TECNICO_INESPERADO);
                }
        }
        // se establecio tipo,numero id,fechas y numero de planilla
        if (operador == null) {
            // se realiza la busqueda por tipo,numero id,fechas y numero de
            // planilla
            try {

                logger.debug(
                        "Inicia consultarArchivosInconsistentesResumen(List<InconsistenciaDTO>, InconsistenciasDTO):Inicia Busqueda por tipo id,id,digito de verificacion y numero de planilla");

                List<Object[]> consultaNativa = entityManager
                        .createNamedQuery(NamedQueriesConstants.CONSULTAR_BANDEJA_CON_ARGUMENTOS_I_IDENTIFICACION_FECHAS_PLANILLA)
                        .setParameter("tipoIdentificacion", tipoIdentificacion.getValorEnPILA())
                        .setParameter("numeroIdentificacion", numeroIdentificacion)
                        .setParameter("fechaInicio", CalendarUtils.truncarHora(fechaI))
                        .setParameter("fechaFin", CalendarUtils.truncarHoraMaxima(fechaF)).setParameter("numeroPlanilla", numeroPlanilla)
                        .getResultList();

                List<InconsistenciaDTO> inconsistenciasI = mapeoInconsistenciaDTO(consultaNativa);
                inconsistenciasI = establecerOperador(inconsistenciasI, "I");
                inconsistenciasI = establecerIdentificacionAportante(inconsistenciasI);
                result.addAll(inconsistenciasI);

                return evaluarResultado(result);
            } catch (Exception e) {
                logger.error("Error al realizar la consulta,verifique los datos", e);
                logger.debug("Finaliza consultarArchivosInconsistentesResumen(List<InconsistenciaDTO>, InconsistenciasDTO)");

                throw new TechnicalException(MensajesGeneralConstants.ERROR_TECNICO_INESPERADO);
            }
        }

        // se realiza la busqueda por tipo,numero id,fechas,numero de
        // planilla y operador de informacion
        if (operador == TipoOperadorEnum.OPERADOR_INFORMACION) {
            try {
                logger.debug(
                        "Inicia consultarArchivosInconsistentesResumen(List<InconsistenciaDTO>, InconsistenciasDTO):Inicia Busqueda por tipo id,id,digito verificacion,fechas,numero de planilla y operador de informacion ");
                List<Object[]> consultaNativa = entityManager
                        .createNamedQuery(NamedQueriesConstants.CONSULTAR_BANDEJA_CON_ARGUMENTOS_I_IDENTIFICACION_FECHAS_PLANILLA)
                        .setParameter("tipoIdentificacion", tipoIdentificacion.getValorEnPILA())
                        .setParameter("numeroIdentificacion", numeroIdentificacion)
                        .setParameter("fechaInicio", CalendarUtils.truncarHora(fechaI))
                        .setParameter("fechaFin", CalendarUtils.truncarHoraMaxima(fechaF)).setParameter("numeroPlanilla", numeroPlanilla)
                        .getResultList();

                List<InconsistenciaDTO> inconsistenciasI = mapeoInconsistenciaDTO(consultaNativa);
                inconsistenciasI = establecerOperador(inconsistenciasI, "I");
                inconsistenciasI = establecerIdentificacionAportante(inconsistenciasI);
                result.addAll(inconsistenciasI);

                return evaluarResultado(result);
            } catch (Exception e) {
                logger.error("Error al realizar la consulta,verifique los datos", e);
                logger.debug("Finaliza consultarArchivosInconsistentesResumen(List<InconsistenciaDTO>, InconsistenciasDTO)");

                throw new TechnicalException(MensajesGeneralConstants.ERROR_TECNICO_INESPERADO);
            }
        }

        logger.debug(
                "Finaliza consultarArchivosInconsistentesResumen(List<InconsistenciaDTO>, InconsistenciasDTO):No se establecio una busqueda compatible con los argumentos esperados");

        return null;

    }

    /**
     * (non-Javadoc)
     * 
     * @see com.asopagos.bandejainconsistencias.service.PilaBandejaService#accionBandejaInconsistencias(com.asopagos.bandejainconsistencias.dto.InconsistenciaDTO)
     */
    @Override
    public List<TipoInconsistenciasEnum> accionBandejaInconsistencias(InconsistenciaDTO inconsistencia) {
        // lista que contendria las pestañas que estarian activas en pantalla
        List<BloqueValidacionEnum> bloques = new ArrayList<BloqueValidacionEnum>();

        logger.debug(
                "Inicia accionBandejaInconsistencias(List<TipoInconsistenciasEnum>, TipoInconsistenciasEnum):Inicia Busqueda de los tipos de inconsistencias");

        try {
            // se diferencia si la consulta de las pestañas va a ser para
            // operadores financieros o de informacion
            if (inconsistencia.getTipoOperador() == TipoOperadorEnum.OPERADOR_INFORMACION) {
                bloques = entityManager
                        .createNamedQuery(NamedQueriesConstants.CONSULTAR_PESTAÑAS_INCONSISTENCIAS, BloqueValidacionEnum.class)
                        .setParameter("idIndicePlanilla", inconsistencia.getIndicePlanilla()).getResultList();

                return generarPestañas(bloques);
            }
            else {
                bloques = entityManager
                        .createNamedQuery(NamedQueriesConstants.CONSULTAR_PESTAÑAS_INCONSISTENCIAS_F, BloqueValidacionEnum.class)
                        .setParameter("idIndicePlanilla", inconsistencia.getIndicePlanilla()).getResultList();

                return generarPestañas(bloques);
            }

        } catch (Exception e) {
            logger.error("No es posible realizar la consulta de pestañas", e);
            logger.debug("Finaliza consultarArchivosInconsistentesResumen(List<TipoInconsistenciasEnum>, TipoInconsistenciasEnum)");

            throw new TechnicalException(MensajesGeneralConstants.ERROR_TECNICO_INESPERADO);
        }

    }

    /**
     * (non-Javadoc)
     * 
     * @see com.asopagos.bandejainconsistencias.service.PilaBandejaService#accionBandejaDetalleInconsistencias(com.asopagos.bandejainconsistencias.dto.InconsistenciaDTO,
     *      com.asopagos.enumeraciones.aportes.TipoInconsistenciasEnum)
     */
    @Override
    public List<InconsistenciaDTO> accionBandejaDetalleInconsistencias(InconsistenciaDTO inconsistencia,
            TipoInconsistenciasEnum tipoInconsistencia) {
        // lista que contendra los datos detallados de las inconsistencias
        List<InconsistenciaDTO> result = new ArrayList<InconsistenciaDTO>();
        logger.debug(
                "Inicia accionBandejaDetalleInconsistencias(InconsistenciaDTO inconsistencia,TipoInconsistenciasEnum tipoInconsistencia):Inicia ");

        try {
            // Se establecen las posibilidades de establecer los detalles del
            // archivo dependiendo del tipo de inconsistencia solicitado
            if (tipoInconsistencia == TipoInconsistenciasEnum.ARCHIVO) {
                // se condiciona dependiendo del tipo de archivo que se haya
                // enviado a este punto
                if (inconsistencia.getTipoArchivo() == TipoArchivoPilaEnum.ARCHIVO_OF) {
                    logger.debug(
                            "Inicia accionBandejaDetalleInconsistencias(InconsistenciaDTO inconsistencia,TipoInconsistenciasEnum tipoInconsistencia):Tipo de inconsistencia archivo ");

                    try {
                        logger.debug(
                                "Inicia accionBandejaDetalleInconsistencias(InconsistenciaDTO inconsistencia,TipoInconsistenciasEnum tipoInconsistencia):Inicia Busqueda por archivo financiero para obtener el detalle ");

                        result.addAll(entityManager
                                .createNamedQuery(NamedQueriesConstants.CONSULTAR_PESTAÑAS_INCONSISTENCIAS_DETALLE_F,
                                        InconsistenciaDTO.class)
                                .setParameter("idIndicePlanilla", inconsistencia.getIndicePlanilla())
                                .setParameter("bloque", BloqueValidacionEnum.BLOQUE_0_OF)
                                .setParameter("tipoArchivo", inconsistencia.getTipoArchivo()).getResultList());
                        return establecerDetalleInconsistencia(result);

                    } catch (Exception e) {

                        logger.error("Error al realizar la consulta,verifique los datos", e);
                        logger.debug(
                                "Finaliza accionBandejaDetalleInconsistencias(InconsistenciaDTO inconsistencia,TipoInconsistenciasEnum tipoInconsistencia)");

                        throw new TechnicalException(MensajesGeneralConstants.ERROR_TECNICO_INESPERADO);
                    }
                }

                try {
                    logger.debug(
                            "Inicia accionBandejaDetalleInconsistencias(InconsistenciaDTO inconsistencia,TipoInconsistenciasEnum tipoInconsistencia):Inicia Busqueda por archivo de informacion ");

                    result.addAll(
                            entityManager
                                    .createNamedQuery(NamedQueriesConstants.CONSULTAR_PESTAÑAS_INCONSISTENCIAS_DETALLE,
                                            InconsistenciaDTO.class)
                                    .setParameter("idIndicePlanilla", inconsistencia.getIndicePlanilla())
                                    .setParameter("bloque", BloqueValidacionEnum.BLOQUE_0_OI)
                                    .setParameter("tipoArchivo", inconsistencia.getTipoArchivo()).getResultList());
                    return establecerDetalleInconsistencia(result);

                } catch (Exception e) {

                    logger.error("Error al realizar la consulta,verifique los datos", e);
                    logger.debug(
                            "Finaliza accionBandejaDetalleInconsistencias(InconsistenciaDTO inconsistencia,TipoInconsistenciasEnum tipoInconsistencia)");

                    throw new TechnicalException(MensajesGeneralConstants.ERROR_TECNICO_INESPERADO);
                }
            }

            if (tipoInconsistencia == TipoInconsistenciasEnum.ESTRUCTURA) {
                logger.debug(
                        "Inicia accionBandejaDetalleInconsistencias(InconsistenciaDTO inconsistencia,TipoInconsistenciasEnum tipoInconsistencia):Tipo de inconsistencia estructura ");

                if (inconsistencia.getTipoArchivo() == TipoArchivoPilaEnum.ARCHIVO_OF) {
                    try {
                        logger.debug(
                                "Inicia accionBandejaDetalleInconsistencias(InconsistenciaDTO inconsistencia,TipoInconsistenciasEnum tipoInconsistencia):Inicia Busqueda por archivo financiero ");

                        result.addAll(entityManager
                                .createNamedQuery(NamedQueriesConstants.CONSULTAR_PESTAÑAS_INCONSISTENCIAS_DETALLE_F,
                                        InconsistenciaDTO.class)
                                .setParameter("idIndicePlanilla", inconsistencia.getIndicePlanilla())
                                .setParameter("bloque", BloqueValidacionEnum.BLOQUE_1_OF)
                                .setParameter("tipoArchivo", inconsistencia.getTipoArchivo()).getResultList());
                        return establecerDetalleInconsistencia(result);

                    } catch (Exception e) {

                        logger.error("Error al realizar la consulta,verifique los datos", e);
                        logger.debug(
                                "Finaliza accionBandejaDetalleInconsistencias(InconsistenciaDTO inconsistencia,TipoInconsistenciasEnum tipoInconsistencia)");

                        throw new TechnicalException(MensajesGeneralConstants.ERROR_TECNICO_INESPERADO);
                    }
                }

                try {
                    logger.debug(
                            "Inicia accionBandejaDetalleInconsistencias(InconsistenciaDTO inconsistencia,TipoInconsistenciasEnum tipoInconsistencia):Inicia Busqueda por archivos informacion ");

                    result.addAll(
                            entityManager
                                    .createNamedQuery(NamedQueriesConstants.CONSULTAR_PESTAÑAS_INCONSISTENCIAS_DETALLE,
                                            InconsistenciaDTO.class)
                                    .setParameter("idIndicePlanilla", inconsistencia.getIndicePlanilla())
                                    .setParameter("bloque", BloqueValidacionEnum.BLOQUE_1_OI)
                                    .setParameter("tipoArchivo", inconsistencia.getTipoArchivo()).getResultList());

                    result.addAll(
                            entityManager
                                    .createNamedQuery(NamedQueriesConstants.CONSULTAR_PESTAÑAS_INCONSISTENCIAS_DETALLE,
                                            InconsistenciaDTO.class)
                                    .setParameter("idIndicePlanilla", inconsistencia.getIndicePlanilla())
                                    .setParameter("bloque", BloqueValidacionEnum.BLOQUE_2_OI)
                                    .setParameter("tipoArchivo", inconsistencia.getTipoArchivo()).getResultList());
                    return establecerDetalleInconsistencia(result);

                } catch (Exception e) {

                    logger.error("Error al realizar la consulta,verifique los datos", e);
                    logger.debug(
                            "Finaliza accionBandejaDetalleInconsistencias(InconsistenciaDTO inconsistencia,TipoInconsistenciasEnum tipoInconsistencia)");

                    throw new TechnicalException(MensajesGeneralConstants.ERROR_TECNICO_INESPERADO);
                }
            }

            if (tipoInconsistencia == TipoInconsistenciasEnum.PAREJA_DE_ARCHIVOS) {
                logger.debug(
                        "Inicia accionBandejaDetalleInconsistencias(InconsistenciaDTO inconsistencia,TipoInconsistenciasEnum tipoInconsistencia):Tipo de inconsistencia pareja de archivos ");

                if (inconsistencia.getEstadoArchivo() == EstadoProcesoArchivoEnum.PAREJA_DE_ARCHIVOS_EN_ESPERA) {

                    return null;
                }

                if ((inconsistencia.getTipoArchivo() == TipoArchivoPilaEnum.ARCHIVO_OI_I)
                        || (inconsistencia.getTipoArchivo() == TipoArchivoPilaEnum.ARCHIVO_OI_A)) {
                    try {
                        logger.debug(
                                "Inicia accionBandejaDetalleInconsistencias(InconsistenciaDTO inconsistencia,TipoInconsistenciasEnum tipoInconsistencia):Inicia Busqueda por archivos I y A");

                        result.addAll(
                                entityManager
                                        .createNamedQuery(NamedQueriesConstants.CONSULTAR_PESTAÑAS_INCONSISTENCIAS_DETALLE,
                                                InconsistenciaDTO.class)
                                        .setParameter("idIndicePlanilla", inconsistencia.getIndicePlanilla())
                                        .setParameter("bloque", BloqueValidacionEnum.BLOQUE_3_OI)
                                        .setParameter("tipoArchivo", TipoArchivoPilaEnum.ARCHIVO_OI_I).getResultList());

                        result.addAll(
                                entityManager
                                        .createNamedQuery(NamedQueriesConstants.CONSULTAR_PESTAÑAS_INCONSISTENCIAS_DETALLE,
                                                InconsistenciaDTO.class)
                                        .setParameter("idIndicePlanilla", inconsistencia.getIndicePlanilla())
                                        .setParameter("bloque", BloqueValidacionEnum.BLOQUE_3_OI)
                                        .setParameter("tipoArchivo", TipoArchivoPilaEnum.ARCHIVO_OI_A).getResultList());
                        return establecerDetalleInconsistencia(result);

                    } catch (Exception e) {

                        logger.error("Error al realizar la consulta,verifique los datos", e);
                        logger.debug(
                                "Finaliza accionBandejaDetalleInconsistencias(InconsistenciaDTO inconsistencia,TipoInconsistenciasEnum tipoInconsistencia)");

                        throw new TechnicalException(MensajesGeneralConstants.ERROR_TECNICO_INESPERADO);
                    }
                }

                if ((inconsistencia.getTipoArchivo() == TipoArchivoPilaEnum.ARCHIVO_OI_IP)
                        || (inconsistencia.getTipoArchivo() == TipoArchivoPilaEnum.ARCHIVO_OI_AP)) {
                    try {
                        logger.debug(
                                "Inicia accionBandejaDetalleInconsistencias(InconsistenciaDTO inconsistencia,TipoInconsistenciasEnum tipoInconsistencia):Inicia Busqueda por archivos IP y AP");

                        result.addAll(
                                entityManager
                                        .createNamedQuery(NamedQueriesConstants.CONSULTAR_PESTAÑAS_INCONSISTENCIAS_DETALLE,
                                                InconsistenciaDTO.class)
                                        .setParameter("idIndicePlanilla", inconsistencia.getIndicePlanilla())
                                        .setParameter("bloque", BloqueValidacionEnum.BLOQUE_3_OI)
                                        .setParameter("tipoArchivo", TipoArchivoPilaEnum.ARCHIVO_OI_IP).getResultList());

                        result.addAll(
                                entityManager
                                        .createNamedQuery(NamedQueriesConstants.CONSULTAR_PESTAÑAS_INCONSISTENCIAS_DETALLE,
                                                InconsistenciaDTO.class)
                                        .setParameter("idIndicePlanilla", inconsistencia.getIndicePlanilla())
                                        .setParameter("bloque", BloqueValidacionEnum.BLOQUE_3_OI)
                                        .setParameter("tipoArchivo", TipoArchivoPilaEnum.ARCHIVO_OI_AP).getResultList());
                        return establecerDetalleInconsistencia(result);

                    } catch (Exception e) {

                        logger.error("Error al realizar la consulta,verifique los datos", e);
                        logger.debug(
                                "Finaliza accionBandejaDetalleInconsistencias(InconsistenciaDTO inconsistencia,TipoInconsistenciasEnum tipoInconsistencia)");

                        throw new TechnicalException(MensajesGeneralConstants.ERROR_TECNICO_INESPERADO);
                    }
                }

                if ((inconsistencia.getTipoArchivo() == TipoArchivoPilaEnum.ARCHIVO_OI_IPR)
                        || (inconsistencia.getTipoArchivo() == TipoArchivoPilaEnum.ARCHIVO_OI_APR)) {
                    try {
                        logger.debug(
                                "Inicia accionBandejaDetalleInconsistencias(InconsistenciaDTO inconsistencia,TipoInconsistenciasEnum tipoInconsistencia):Inicia Busqueda por archivo IPR y APR ");

                        result.addAll(
                                entityManager
                                        .createNamedQuery(NamedQueriesConstants.CONSULTAR_PESTAÑAS_INCONSISTENCIAS_DETALLE,
                                                InconsistenciaDTO.class)
                                        .setParameter("idIndicePlanilla", inconsistencia.getIndicePlanilla())
                                        .setParameter("bloque", BloqueValidacionEnum.BLOQUE_3_OI)
                                        .setParameter("tipoArchivo", TipoArchivoPilaEnum.ARCHIVO_OI_IPR).getResultList());

                        result.addAll(
                                entityManager
                                        .createNamedQuery(NamedQueriesConstants.CONSULTAR_PESTAÑAS_INCONSISTENCIAS_DETALLE,
                                                InconsistenciaDTO.class)
                                        .setParameter("idIndicePlanilla", inconsistencia.getIndicePlanilla())
                                        .setParameter("bloque", BloqueValidacionEnum.BLOQUE_3_OI)
                                        .setParameter("tipoArchivo", TipoArchivoPilaEnum.ARCHIVO_OI_APR).getResultList());
                        return establecerDetalleInconsistencia(result);

                    } catch (Exception e) {

                        logger.error("Error al realizar la consulta,verifique los datos", e);
                        logger.debug(
                                "Finaliza accionBandejaDetalleInconsistencias(InconsistenciaDTO inconsistencia,TipoInconsistenciasEnum tipoInconsistencia)");

                        throw new TechnicalException(MensajesGeneralConstants.ERROR_TECNICO_INESPERADO);
                    }
                }

                if ((inconsistencia.getTipoArchivo() == TipoArchivoPilaEnum.ARCHIVO_OI_IR)
                        || (inconsistencia.getTipoArchivo() == TipoArchivoPilaEnum.ARCHIVO_OI_AR)) {
                    try {
                        logger.debug(
                                "Inicia accionBandejaDetalleInconsistencias(InconsistenciaDTO inconsistencia,TipoInconsistenciasEnum tipoInconsistencia):Inicia Busqueda por archivo IR y AR");

                        result.addAll(
                                entityManager
                                        .createNamedQuery(NamedQueriesConstants.CONSULTAR_PESTAÑAS_INCONSISTENCIAS_DETALLE,
                                                InconsistenciaDTO.class)
                                        .setParameter("idIndicePlanilla", inconsistencia.getIndicePlanilla())
                                        .setParameter("bloque", BloqueValidacionEnum.BLOQUE_3_OI)
                                        .setParameter("tipoArchivo", TipoArchivoPilaEnum.ARCHIVO_OI_IR).getResultList());

                        result.addAll(
                                entityManager
                                        .createNamedQuery(NamedQueriesConstants.CONSULTAR_PESTAÑAS_INCONSISTENCIAS_DETALLE,
                                                InconsistenciaDTO.class)
                                        .setParameter("idIndicePlanilla", inconsistencia.getIndicePlanilla())
                                        .setParameter("bloque", BloqueValidacionEnum.BLOQUE_3_OI)
                                        .setParameter("tipoArchivo", TipoArchivoPilaEnum.ARCHIVO_OI_AR).getResultList());
                        return establecerDetalleInconsistencia(result);

                    } catch (Exception e) {

                        logger.error("Error al realizar la consulta,verifique los datos", e);
                        logger.debug(
                                "Finaliza accionBandejaDetalleInconsistencias(InconsistenciaDTO inconsistencia,TipoInconsistenciasEnum tipoInconsistencia)");

                        throw new TechnicalException(MensajesGeneralConstants.ERROR_TECNICO_INESPERADO);
                    }
                }
            }

            if (tipoInconsistencia == TipoInconsistenciasEnum.APORTANTE_NO_IDENTIFICADO) {
                logger.debug(
                        "Inicia accionBandejaDetalleInconsistencias(InconsistenciaDTO inconsistencia,TipoInconsistenciasEnum tipoInconsistencia):Tipo de inconsistencia aportante no identificado ");

                try {
                    logger.debug(
                            "Inicia accionBandejaDetalleInconsistencias(InconsistenciaDTO inconsistencia,TipoInconsistenciasEnum tipoInconsistencia):Inicia busqueda de las inconsistencias de aportante no identificado ");

                    result.addAll(
                            entityManager
                                    .createNamedQuery(NamedQueriesConstants.CONSULTAR_PESTAÑAS_INCONSISTENCIAS_DETALLE,
                                            InconsistenciaDTO.class)
                                    .setParameter("idIndicePlanilla", inconsistencia.getIndicePlanilla())
                                    .setParameter("bloque", BloqueValidacionEnum.BLOQUE_5_OI)
                                    .setParameter("tipoArchivo", inconsistencia.getTipoArchivo()).getResultList());
                    return establecerDetalleInconsistencia(result);

                } catch (Exception e) {

                    logger.error("Error al realizar la consulta,verifique los datos", e);
                    logger.debug(
                            "Finaliza accionBandejaDetalleInconsistencias(InconsistenciaDTO inconsistencia,TipoInconsistenciasEnum tipoInconsistencia)");

                    throw new TechnicalException(MensajesGeneralConstants.ERROR_TECNICO_INESPERADO);
                }
            }

            if (tipoInconsistencia == TipoInconsistenciasEnum.CONCILIACION) {
                logger.debug(
                        "Inicia accionBandejaDetalleInconsistencias(InconsistenciaDTO inconsistencia,TipoInconsistenciasEnum tipoInconsistencia):Tipo de inconsistencia conciliacion ");

                try {
                    logger.debug(
                            "Inicia accionBandejaDetalleInconsistencias(InconsistenciaDTO inconsistencia,TipoInconsistenciasEnum tipoInconsistencia):Inicia Busqueda para conciliacion ");
                    PilaArchivoFRegistro6 registroF = entityManager
                            .createNamedQuery(NamedQueriesConstants.BUSQUEDA_INDICE_PLANILLA_CON_NUMERO_PLANILLA_F,
                                    PilaArchivoFRegistro6.class)
                            .setParameter("numeroPlanilla", "" + inconsistencia.getNumeroPlanilla()).getSingleResult();

                    if (registroF != null) {
                        result.addAll(
                                entityManager
                                        .createNamedQuery(NamedQueriesConstants.CONSULTAR_PESTAÑAS_INCONSISTENCIAS_DETALLE,
                                                InconsistenciaDTO.class)
                                        .setParameter("idIndicePlanilla", inconsistencia.getIndicePlanilla())
                                        .setParameter("bloque", BloqueValidacionEnum.BLOQUE_6_OI)
                                        .setParameter("tipoArchivo", inconsistencia.getTipoArchivo()).getResultList());
                        result = establecerDetalleInconsistencia(result);
                        return establecerCamposConciliacion(result);
                    }
                    else {
                        return null;
                    }

                } catch (Exception e) {

                    logger.error("Error al realizar la consulta,verifique los datos", e);
                    logger.debug(
                            "Finaliza accionBandejaDetalleInconsistencias(InconsistenciaDTO inconsistencia,TipoInconsistenciasEnum tipoInconsistencia)");

                    throw new TechnicalException(MensajesGeneralConstants.ERROR_TECNICO_INESPERADO);
                }
            }

            return null;
        } catch (Exception e) {
            logger.error("No es posible realizar la consulta de planillas", e);
            logger.debug("Finaliza consultarArchivosInconsistentesResumen(List<InconsistenciaDTO>, InconsistenciasDTO)");

            throw new TechnicalException(MensajesGeneralConstants.ERROR_TECNICO_INESPERADO);
        }

    }

    /**
     * (non-Javadoc)
     * 
     * @see com.asopagos.bandejainconsistencias.service.PilaBandejaService#anularPlanillaOI(com.asopagos.bandejainconsistencias.dto.InconsistenciaDTO,
     *      com.asopagos.rest.security.dto.UserDTO)
     */
    @Override
    public void anularPlanillaOI(InconsistenciaDTO inconsistencia, UserDTO user) {

        try {
            logger.debug("Inicia anularPlanillaOI(InconsistenciaDTO, inconsistencia)");
            // se obtiene los indices asociados a un numero de planilla
            List<Long> indices = entityManager.createNamedQuery(NamedQueriesConstants.CONSULTAR_ID_INDICES_PLANILLAS, Long.class)
                    .setParameter("numeroPlanilla", inconsistencia.getNumeroPlanilla()).getResultList();
            // si no existe ninguno se genera una excepcion
            if (indices.isEmpty() == true) {
                throw new IllegalArgumentException("No se reconoce el numero de planilla");
            }
            // se valida si se encontraron 2 indices ,esto indica que tiene su
            // pareja el archivo
            if (indices.size() == 2) {

                IndicePlanilla indiceUno = entityManager
                        .createNamedQuery(NamedQueriesConstants.OBTENER_INDICE_PLANILLAS, IndicePlanilla.class)
                        .setParameter("idIndicePlanilla", indices.get(0)).getSingleResult();
                indiceUno.setEstadoArchivo(EstadoProcesoArchivoEnum.ANULADO);
                indiceUno.setRegistroActivo(false);

                EstadoArchivoPorBloque estadoUno = entityManager
                        .createNamedQuery(NamedQueriesConstants.BUSQUEDA_BLOQUE_INDICE, EstadoArchivoPorBloque.class)
                        .setParameter("idIndicePlanilla", indices.get(0)).getSingleResult();

                IndicePlanilla indiceDos = entityManager
                        .createNamedQuery(NamedQueriesConstants.OBTENER_INDICE_PLANILLAS, IndicePlanilla.class)
                        .setParameter("idIndicePlanilla", indices.get(1)).getSingleResult();
                indiceDos.setEstadoArchivo(EstadoProcesoArchivoEnum.ANULADO);
                indiceDos.setRegistroActivo(false);
                EstadoArchivoPorBloque estadoDos = entityManager
                        .createNamedQuery(NamedQueriesConstants.BUSQUEDA_BLOQUE_INDICE, EstadoArchivoPorBloque.class)
                        .setParameter("idIndicePlanilla", indices.get(0)).getSingleResult();

                if (establecerGestionInconsistencias(inconsistencia.getIdErrorValidacion())) {
                    entityManager.merge(establecerUbicacionBloqueAnular(inconsistencia.getBloque(), estadoUno));
                    entityManager.merge(indiceUno);
                    entityManager.merge(establecerUbicacionBloqueAnular(inconsistencia.getBloque(), estadoDos));
                    entityManager.merge(indiceDos);
                }
                else {
                    throw new TechnicalException(MensajesGeneralConstants.ERROR_TECNICO_INESPERADO);
                }

            }
            // en caso de que solo exista un indice se procede a anularlo
            else {

                IndicePlanilla indiceUnico = entityManager
                        .createNamedQuery(NamedQueriesConstants.OBTENER_INDICE_PLANILLAS, IndicePlanilla.class)
                        .setParameter("idIndicePlanilla", indices.get(0)).getSingleResult();
                indiceUnico.setEstadoArchivo(EstadoProcesoArchivoEnum.ANULADO);
                indiceUnico.setRegistroActivo(false);

                EstadoArchivoPorBloque estadoUnico = entityManager
                        .createNamedQuery(NamedQueriesConstants.BUSQUEDA_BLOQUE_INDICE, EstadoArchivoPorBloque.class)
                        .setParameter("idIndicePlanilla", indices.get(0)).getSingleResult();

                if (establecerGestionInconsistencias(inconsistencia.getIdErrorValidacion())) {
                    entityManager.merge(establecerUbicacionBloqueAnular(inconsistencia.getBloque(), estadoUnico));
                    entityManager.merge(indiceUnico);
                }
                else {
                    throw new TechnicalException(MensajesGeneralConstants.ERROR_TECNICO_INESPERADO);
                }
            }
            logger.debug("Finaliza anularPlanilla(InconsistenciaDTO, inconsistencia)");
        } catch (Exception e) {
            logger.error("No es posible realizar la actualizacion de la planilla", e);
            logger.debug("Finaliza anularPlanilla(InconsistenciaDTO, inconsistencia)");
            throw new TechnicalException(MensajesGeneralConstants.ERROR_TECNICO_INESPERADO);
        }

    }

    /**
     * (non-Javadoc)
     * 
     * @see com.asopagos.bandejainconsistencias.service.PilaBandejaService#validarEstructuraPlanilla(com.asopagos.bandejainconsistencias.dto.InconsistenciaDTO)
     */
    @Override
    public void validarEstructuraPlanilla(InconsistenciaDTO inconsistencia) {

        try {
            logger.debug("Inicia anularPlanilla(InconsistenciaDTO, inconsistencia)");
            // se realiza la busqueda de los indices asociados al numero de
            // planilla
            List<Long> indices = entityManager.createNamedQuery(NamedQueriesConstants.CONSULTAR_ID_INDICES_PLANILLAS, Long.class)
                    .setParameter("numeroPlanilla", inconsistencia.getNumeroPlanilla()).getResultList();
            // si no existe ninguno se genera una excepcion
            if (indices.isEmpty() == true) {
                throw new IllegalArgumentException("No se reconoce el numero de planilla");
            }
            // se valida la existencia del archivo par
            if (indices.size() == 2) {

                IndicePlanilla indiceUno = entityManager
                        .createNamedQuery(NamedQueriesConstants.OBTENER_INDICE_PLANILLAS, IndicePlanilla.class)
                        .setParameter("idIndicePlanilla", indices.get(0)).getSingleResult();
                indiceUno.setEstadoArchivo(EstadoProcesoArchivoEnum.ESTRUCTURA_VALIDADA);

                EstadoArchivoPorBloque estadoUno = entityManager
                        .createNamedQuery(NamedQueriesConstants.BUSQUEDA_BLOQUE_INDICE, EstadoArchivoPorBloque.class)
                        .setParameter("idIndicePlanilla", indices.get(0)).getSingleResult();

                IndicePlanilla indiceDos = entityManager
                        .createNamedQuery(NamedQueriesConstants.OBTENER_INDICE_PLANILLAS, IndicePlanilla.class)
                        .setParameter("idIndicePlanilla", indices.get(1)).getSingleResult();
                indiceDos.setEstadoArchivo(EstadoProcesoArchivoEnum.ESTRUCTURA_VALIDADA);

                EstadoArchivoPorBloque estadoDos = entityManager
                        .createNamedQuery(NamedQueriesConstants.BUSQUEDA_BLOQUE_INDICE, EstadoArchivoPorBloque.class)
                        .setParameter("idIndicePlanilla", indices.get(1)).getSingleResult();

                if (establecerGestionInconsistencias(inconsistencia.getIdErrorValidacion())) {
                    entityManager.merge(establecerUbicacionBloqueSiguienteBloque(inconsistencia.getBloque(), estadoUno));
                    entityManager.merge(indiceUno);
                    entityManager.merge(establecerUbicacionBloqueSiguienteBloque(inconsistencia.getBloque(), estadoDos));
                    entityManager.merge(indiceDos);

                }
                else {
                    throw new TechnicalException(MensajesGeneralConstants.ERROR_TECNICO_INESPERADO);
                }

            }
            // dado el caso que no tenga pareja se anula solo un indice
            else {

                IndicePlanilla indiceUnico = entityManager
                        .createNamedQuery(NamedQueriesConstants.OBTENER_INDICE_PLANILLAS, IndicePlanilla.class)
                        .setParameter("idIndicePlanilla", indices.get(0)).getSingleResult();
                indiceUnico.setEstadoArchivo(EstadoProcesoArchivoEnum.ESTRUCTURA_VALIDADA);

                EstadoArchivoPorBloque estadoUnico = entityManager
                        .createNamedQuery(NamedQueriesConstants.BUSQUEDA_BLOQUE_INDICE, EstadoArchivoPorBloque.class)
                        .setParameter("idIndicePlanilla", indices.get(0)).getSingleResult();

                if (establecerGestionInconsistencias(inconsistencia.getIdErrorValidacion())) {
                    entityManager.merge(establecerUbicacionBloqueSiguienteBloque(inconsistencia.getBloque(), estadoUnico));
                    entityManager.merge(indiceUnico);

                }
                else {
                    throw new TechnicalException(MensajesGeneralConstants.ERROR_TECNICO_INESPERADO);
                }
            }
            logger.debug("Finaliza anularPlanilla(InconsistenciaDTO, inconsistencia)");
        } catch (NoResultException nre) {
            logger.debug(
                    "Finaliza anularPlanilla(InconsistenciaDTO, inconsistencia):No se pudo actualizar la planilla, recursos no encontrados");
        } catch (Exception e) {
            logger.error("No es posible realizar la actualizacion de la planilla", e);
            logger.debug("Finaliza anularPlanilla(InconsistenciaDTO, inconsistencia)");
            throw new TechnicalException(MensajesGeneralConstants.ERROR_TECNICO_INESPERADO);
        }

    }

    /**
     * (non-Javadoc)
     * 
     * @see com.asopagos.bandejainconsistencias.service.PilaBandejaService#anularPlanillaOF(com.asopagos.bandejainconsistencias.dto.InconsistenciaDTO)
     */
    @Override
    public void anularPlanillaOF(InconsistenciaDTO inconsistencia) {

        try {
            logger.debug("Inicia anularPlanilla(InconsistenciaDTO, inconsistencia)");
            // se realiza la busqueda de los indices asociados al
            List<Long> indices = entityManager.createNamedQuery(NamedQueriesConstants.CONSULTAR_ID_INDICES_PLANILLAS_OF, Long.class)
                    .setParameter("numeroPlanilla", inconsistencia.getIndicePlanilla()).getResultList();

            if (indices.isEmpty() == true) {
                throw new IllegalArgumentException("No se reconoce el numero de planilla");
            }
            // se modiica el unico archivo OF y se establece como anulado
            IndicePlanillaOF indiceUnico = entityManager
                    .createNamedQuery(NamedQueriesConstants.OBTENER_INDICE_PLANILLAS_OF, IndicePlanillaOF.class)
                    .setParameter("idIndicePlanilla", indices.get(0)).getSingleResult();
            indiceUnico.setEstado(EstadoProcesoArchivoEnum.ANULADO);
            indiceUnico.setRegistroActivo(false);

            EstadoArchivoPorBloqueOF estadoUnico = entityManager
                    .createNamedQuery(NamedQueriesConstants.BUSQUEDA_BLOQUE_INDICE_OF, EstadoArchivoPorBloqueOF.class)
                    .setParameter("idIndicePlanilla", indices.get(0)).getSingleResult();
            // se establece como gestionada la inconsistencias asociada
            if (establecerGestionInconsistencias(inconsistencia.getIdErrorValidacion())) {
                entityManager.merge(establecerUbicacionBloqueAnularOF(inconsistencia.getBloque(), estadoUnico));
                if (indiceUnico != null) {
                    entityManager.merge(indiceUnico);
                }

            }
            else {
                throw new TechnicalException(MensajesGeneralConstants.ERROR_TECNICO_INESPERADO);
            }

            logger.debug("Finaliza anularPlanilla(InconsistenciaDTO, inconsistencia)");
        } catch (Exception e) {
            logger.error("No es posible realizar la actualizacion de la planilla", e);
            logger.debug("Finaliza anularPlanilla(InconsistenciaDTO, inconsistencia)");
            throw new TechnicalException(MensajesGeneralConstants.ERROR_TECNICO_INESPERADO);
        }

    }

    /**
     * (non-Javadoc)
     * 
     * @see com.asopagos.bandejainconsistencias.service.PilaBandejaService#enviarSolicitudCambioIden(com.asopagos.bandejainconsistencias.dto.InconsistenciaDTO,
     *      java.lang.Long, com.asopagos.rest.security.dto.UserDTO)
     */
    @Override
    public void enviarSolicitudCambioIden(InconsistenciaDTO inconsistencia, Long numeroIdentificacion, UserDTO user) {

        logger.debug("Inicia enviarSolicitudCambioIden(BandejaSolicitudCambioIden, solicitudCambio)");
        // se genera una nueva solicitu de correcion
        IndiceCorreccionPlanilla solicitud = new IndiceCorreccionPlanilla();
        // se establecen las posibles planillas que se verian afectadas
        IndicePlanilla indiceI = new IndicePlanilla();
        IndicePlanilla indiceA = new IndicePlanilla();
        IndicePlanillaOF indiceF = new IndicePlanillaOF();

        String numeroPlanillaOF = "" + inconsistencia.getNumeroPlanilla();
        // se realiza la busqueda dependiendo del tipo de archivo
        if ((inconsistencia.getTipoArchivo() == TipoArchivoPilaEnum.ARCHIVO_OI_I)
                || (inconsistencia.getTipoArchivo() == TipoArchivoPilaEnum.ARCHIVO_OI_A)) {
            try {
                logger.debug(
                        "Inicia enviarSolicitudCambioIden(BandejaSolicitudCambioIden, solicitudCambio):Inicia Busqueda por tipo de archivo I o A");

                indiceI = entityManager
                        .createNamedQuery(NamedQueriesConstants.CONSULTAR_ARCHIVOS_ASOCIADOS_INFORMACION, IndicePlanilla.class)
                        .setParameter("numeroPlanilla", inconsistencia.getNumeroPlanilla())
                        .setParameter("tipoArchivo", TipoArchivoPilaEnum.ARCHIVO_OI_I).getSingleResult();

                indiceA = entityManager
                        .createNamedQuery(NamedQueriesConstants.CONSULTAR_ARCHIVOS_ASOCIADOS_INFORMACION, IndicePlanilla.class)
                        .setParameter("numeroPlanilla", inconsistencia.getNumeroPlanilla())
                        .setParameter("tipoArchivo", TipoArchivoPilaEnum.ARCHIVO_OI_A).getSingleResult();

                indiceF = entityManager
                        .createNamedQuery(NamedQueriesConstants.CONSULTAR_ARCHIVOS_ASOCIADOS_FINANCIERO, IndicePlanillaOF.class)
                        .setParameter("numeroPlanilla", numeroPlanillaOF).getSingleResult();
            } catch (Exception e) {
                logger.error("Error al realizar la consulta,verifique los datos", e);
                logger.debug("Finaliza enviarSolicitudCambioIden(BandejaSolicitudCambioIden, solicitudCambio)");
                throw new TechnicalException(MensajesGeneralConstants.ERROR_TECNICO_INESPERADO);
            }
        }

        if ((inconsistencia.getTipoArchivo() == TipoArchivoPilaEnum.ARCHIVO_OI_IR)
                || (inconsistencia.getTipoArchivo() == TipoArchivoPilaEnum.ARCHIVO_OI_AR)) {
            try {
                logger.debug(
                        "Inicia enviarSolicitudCambioIden(BandejaSolicitudCambioIden, solicitudCambio):Inicia Busqueda por archivo IR y AR ");

                indiceI = entityManager
                        .createNamedQuery(NamedQueriesConstants.CONSULTAR_ARCHIVOS_ASOCIADOS_INFORMACION, IndicePlanilla.class)
                        .setParameter("numeroPlanilla", inconsistencia.getNumeroPlanilla())
                        .setParameter("tipoArchivo", TipoArchivoPilaEnum.ARCHIVO_OI_IR).getSingleResult();

                indiceA = entityManager
                        .createNamedQuery(NamedQueriesConstants.CONSULTAR_ARCHIVOS_ASOCIADOS_INFORMACION, IndicePlanilla.class)
                        .setParameter("numeroPlanilla", inconsistencia.getNumeroPlanilla())
                        .setParameter("tipoArchivo", TipoArchivoPilaEnum.ARCHIVO_OI_AR).getSingleResult();

                indiceF = entityManager
                        .createNamedQuery(NamedQueriesConstants.CONSULTAR_ARCHIVOS_ASOCIADOS_FINANCIERO, IndicePlanillaOF.class)
                        .setParameter("numeroPlanilla", numeroPlanillaOF).getSingleResult();
            } catch (Exception e) {
                logger.error("Error al realizar la consulta,verifique los datos", e);
                logger.debug("Finaliza enviarSolicitudCambioIden(BandejaSolicitudCambioIden, solicitudCambio)");
                throw new TechnicalException(MensajesGeneralConstants.ERROR_TECNICO_INESPERADO);
            }
        }

        if ((inconsistencia.getTipoArchivo() == TipoArchivoPilaEnum.ARCHIVO_OI_IP)
                || (inconsistencia.getTipoArchivo() == TipoArchivoPilaEnum.ARCHIVO_OI_AP)) {
            try {
                logger.debug(
                        "Inicia enviarSolicitudCambioIden(BandejaSolicitudCambioIden, solicitudCambio):Inicia Busqueda por archivo IP y AP ");

                indiceI = entityManager
                        .createNamedQuery(NamedQueriesConstants.CONSULTAR_ARCHIVOS_ASOCIADOS_INFORMACION, IndicePlanilla.class)
                        .setParameter("numeroPlanilla", inconsistencia.getNumeroPlanilla())
                        .setParameter("tipoArchivo", TipoArchivoPilaEnum.ARCHIVO_OI_IP).getSingleResult();

                indiceA = entityManager
                        .createNamedQuery(NamedQueriesConstants.CONSULTAR_ARCHIVOS_ASOCIADOS_INFORMACION, IndicePlanilla.class)
                        .setParameter("numeroPlanilla", inconsistencia.getNumeroPlanilla())
                        .setParameter("tipoArchivo", TipoArchivoPilaEnum.ARCHIVO_OI_AP).getSingleResult();

                indiceF = entityManager
                        .createNamedQuery(NamedQueriesConstants.CONSULTAR_ARCHIVOS_ASOCIADOS_FINANCIERO, IndicePlanillaOF.class)
                        .setParameter("numeroPlanilla", numeroPlanillaOF).getSingleResult();
            } catch (Exception e) {
                logger.error("Error al realizar la consulta,verifique los datos", e);
                logger.debug("Finaliza enviarSolicitudCambioIden(BandejaSolicitudCambioIden, solicitudCambio)");
                throw new TechnicalException(MensajesGeneralConstants.ERROR_TECNICO_INESPERADO);
            }
        }

        if ((inconsistencia.getTipoArchivo() == TipoArchivoPilaEnum.ARCHIVO_OI_IPR)
                || (inconsistencia.getTipoArchivo() == TipoArchivoPilaEnum.ARCHIVO_OI_APR)) {
            try {
                logger.debug(
                        "Inicia enviarSolicitudCambioIden(BandejaSolicitudCambioIden, solicitudCambio):Inicia Busqueda por archivo IPR y APR");

                indiceI = entityManager
                        .createNamedQuery(NamedQueriesConstants.CONSULTAR_ARCHIVOS_ASOCIADOS_INFORMACION, IndicePlanilla.class)
                        .setParameter("numeroPlanilla", inconsistencia.getNumeroPlanilla())
                        .setParameter("tipoArchivo", TipoArchivoPilaEnum.ARCHIVO_OI_IPR).getSingleResult();

                indiceA = entityManager
                        .createNamedQuery(NamedQueriesConstants.CONSULTAR_ARCHIVOS_ASOCIADOS_INFORMACION, IndicePlanilla.class)
                        .setParameter("numeroPlanilla", inconsistencia.getNumeroPlanilla())
                        .setParameter("tipoArchivo", TipoArchivoPilaEnum.ARCHIVO_OI_APR).getSingleResult();

                indiceF = entityManager
                        .createNamedQuery(NamedQueriesConstants.CONSULTAR_ARCHIVOS_ASOCIADOS_FINANCIERO, IndicePlanillaOF.class)
                        .setParameter("numeroPlanilla", numeroPlanillaOF).getSingleResult();
            } catch (Exception e) {
                logger.error("Error al realizar la consulta,verifique los datos", e);
                logger.debug("Finaliza enviarSolicitudCambioIden(BandejaSolicitudCambioIden, solicitudCambio)");
                throw new TechnicalException(MensajesGeneralConstants.ERROR_TECNICO_INESPERADO);
            }
        }
        // al finalizar se establecen los datos finales de la solicitud
        solicitud.setIndicePlanilla(
                entityManager.createNamedQuery(NamedQueriesConstants.CONSULTAR_ARCHIVOS_ASOCIADOS_INFORMACION, IndicePlanilla.class)
                        .setParameter("numeroPlanilla", inconsistencia.getNumeroPlanilla())
                        .setParameter("tipoArchivo", inconsistencia.getTipoArchivo()).getSingleResult());

        if (solicitud.getIndicePlanilla().getTipoArchivo() == indiceI.getTipoArchivo()) {
            solicitud.setIdPlanillaInformacion(indiceA.getId());
        }
        else {
            solicitud.setIdPlanillaInformacion(indiceI.getId());
        }

        solicitud.setIdPlanillaFinanciera(indiceF.getId());
        solicitud.setAccionCorreccion(AccionCorreccionPilaEnum.REGISTRAR_SOLICITUD_CAMBIO_IDENTIFICACION);
        solicitud.setArchivosCorrecion(generarStringArchivosAsociados(indiceI, indiceA, indiceF));
        solicitud.setFechaSolicitud(new Date());
        solicitud.setNumeroIdentificacion(numeroIdentificacion);
        solicitud.setUsuarioSolicitud(user.getEmail());

        if (establecerGestionInconsistencias(inconsistencia.getIdErrorValidacion())) {
            entityManager.persist(solicitud);
        }
        else {
            throw new TechnicalException(MensajesGeneralConstants.ERROR_TECNICO_INESPERADO);
        }

    }

    /**
     * (non-Javadoc)
     * 
     * @see com.asopagos.bandejainconsistencias.service.PilaBandejaService#aprobarSolicitudCambioIden(java.util.List,
     *      com.asopagos.rest.security.dto.UserDTO)
     */
    @Override
    public ResultadoAprobacionCambioIdentificacionDTO aprobarSolicitudCambioIden(List<IndiceCorreccionPlanilla> listaSolicitudes,
            UserDTO user) {
        // se establecen los datos iniciales para aprobar la solicitud
        ResultadoAprobacionCambioIdentificacionDTO result = new ResultadoAprobacionCambioIdentificacionDTO();
        List<IndicePlanilla> indicesOI = new ArrayList<IndicePlanilla>();
        List<IndicePlanillaOF> indicesOF = new ArrayList<IndicePlanillaOF>();
        // se recorre la lista de solicitudes seleccionada por el supervisor de
        // aportes para ser aprobadas
        for (IndiceCorreccionPlanilla solicitud : listaSolicitudes) {
            solicitud.setAccionCorreccion(AccionCorreccionPilaEnum.REGISTRAR_RESPUESTA_CAMBIO_IDENTIFICACION);
            solicitud.setFechaRespuesta(new Date());
            solicitud.setUsuarioAprobador(user.getEmail());
            // Se busca que tipo de archivo fue por el que se inicio la
            // solicitud de cambio de identificacion y se modifican los datos
            // para posteriormente persistirlos y dar como aprobada la solicitud
            if (solicitud.getIndicePlanilla().getTipoArchivo() == TipoArchivoPilaEnum.ARCHIVO_OI_I) {

                PilaArchivoIRegistro1 registroi = entityManager
                        .createNamedQuery(NamedQueriesConstants.BUSQUEDA_INDICE_PLANILLA_I, PilaArchivoIRegistro1.class)
                        .setParameter("idIndicePlanilla", solicitud.getIndicePlanilla().getId()).getSingleResult();

                String cambioNombreArchivo = modificarNombreArchivo(solicitud.getNumeroIdentificacion(), registroi.getIdAportante(),
                        solicitud.getIndicePlanilla().getNombreArchivo());

                EstadoArchivoPorBloque estadoI = entityManager
                        .createNamedQuery(NamedQueriesConstants.BUSQUEDA_BLOQUE_INDICE, EstadoArchivoPorBloque.class)
                        .setParameter("idIndicePlanilla", solicitud.getIndicePlanilla().getId()).getSingleResult();

                solicitud.getIndicePlanilla().setNombreArchivo(cambioNombreArchivo);
                registroi.setIdAportante("" + solicitud.getNumeroIdentificacion());
                solicitud.getIndicePlanilla().setEstadoArchivo(EstadoProcesoArchivoEnum.APROBADO);
                estadoI.setEstadoBloque5(EstadoProcesoArchivoEnum.APROBADO);
                estadoI.setAccionBloque5(AccionProcesoArchivoEnum.REINTENTAR_BLOQUE);

                PilaArchivoARegistro1 registroa = entityManager
                        .createNamedQuery(NamedQueriesConstants.BUSQUEDA_INDICE_PLANILLA_A, PilaArchivoARegistro1.class)
                        .setParameter("idIndicePlanilla", solicitud.getIdPlanillaInformacion()).getSingleResult();

                IndicePlanilla indiceA = entityManager
                        .createNamedQuery(NamedQueriesConstants.CONSULTAR_ARCHIVOS_ASOCIADOS_INFORMACION, IndicePlanilla.class)
                        .setParameter("numeroPlanilla", solicitud.getIndicePlanilla().getIdPlanilla())
                        .setParameter("tipoArchivo", TipoArchivoPilaEnum.ARCHIVO_OI_A).getSingleResult();

                cambioNombreArchivo = modificarNombreArchivo(solicitud.getNumeroIdentificacion(), registroa.getIdAportante(),
                        indiceA.getNombreArchivo());

                EstadoArchivoPorBloque estadoA = entityManager
                        .createNamedQuery(NamedQueriesConstants.BUSQUEDA_BLOQUE_INDICE, EstadoArchivoPorBloque.class)
                        .setParameter("idIndicePlanilla", solicitud.getIdPlanillaInformacion()).getSingleResult();

                registroa.setIdAportante("" + solicitud.getNumeroIdentificacion());
                indiceA.setNombreArchivo(cambioNombreArchivo);
                indiceA.setEstadoArchivo(EstadoProcesoArchivoEnum.APROBADO);
                estadoA.setEstadoBloque5(EstadoProcesoArchivoEnum.APROBADO);
                estadoA.setAccionBloque5(AccionProcesoArchivoEnum.REINTENTAR_BLOQUE);

                List<IndicePlanillaOF> indiceF = entityManager
                        .createNamedQuery(NamedQueriesConstants.CONSULTAR_ARCHIVOS_ASOCIADOS_FINANCIERO, IndicePlanillaOF.class)
                        .setParameter("numeroPlanilla", "" + solicitud.getIndicePlanilla().getIdPlanilla()).getResultList();

                indicesOI.add(solicitud.getIndicePlanilla());
                indicesOF.add(indiceF.get(0));
                entityManager.merge(solicitud);
                entityManager.merge(solicitud.getIndicePlanilla());
                entityManager.merge(indiceA);
                entityManager.merge(registroi);
                entityManager.merge(registroa);
                entityManager.merge(estadoI);
                entityManager.merge(estadoA);
            }

            if (solicitud.getIndicePlanilla().getTipoArchivo() == TipoArchivoPilaEnum.ARCHIVO_OI_A) {

                PilaArchivoIRegistro1 registroi = entityManager
                        .createNamedQuery(NamedQueriesConstants.BUSQUEDA_INDICE_PLANILLA_I, PilaArchivoIRegistro1.class)
                        .setParameter("idIndicePlanilla", solicitud.getIndicePlanilla().getId()).getSingleResult();

                IndicePlanilla indiceI = entityManager
                        .createNamedQuery(NamedQueriesConstants.CONSULTAR_ARCHIVOS_ASOCIADOS_INFORMACION, IndicePlanilla.class)
                        .setParameter("numeroPlanilla", solicitud.getIndicePlanilla().getIdPlanilla())
                        .setParameter("tipoArchivo", TipoArchivoPilaEnum.ARCHIVO_OI_I).getSingleResult();

                String cambioNombreArchivo = modificarNombreArchivo(solicitud.getNumeroIdentificacion(), registroi.getIdAportante(),
                        indiceI.getNombreArchivo());

                EstadoArchivoPorBloque estadoI = entityManager
                        .createNamedQuery(NamedQueriesConstants.BUSQUEDA_BLOQUE_INDICE, EstadoArchivoPorBloque.class)
                        .setParameter("idIndicePlanilla", solicitud.getIndicePlanilla().getId()).getSingleResult();

                indiceI.setNombreArchivo(cambioNombreArchivo);
                indiceI.setEstadoArchivo(EstadoProcesoArchivoEnum.APROBADO);
                registroi.setIdAportante("" + solicitud.getNumeroIdentificacion());
                estadoI.setEstadoBloque5(EstadoProcesoArchivoEnum.APROBADO);
                estadoI.setAccionBloque5(AccionProcesoArchivoEnum.REINTENTAR_BLOQUE);

                PilaArchivoARegistro1 registroa = entityManager
                        .createNamedQuery(NamedQueriesConstants.BUSQUEDA_INDICE_PLANILLA_A, PilaArchivoARegistro1.class)
                        .setParameter("idIndicePlanilla", solicitud.getIdPlanillaInformacion()).getSingleResult();

                cambioNombreArchivo = modificarNombreArchivo(solicitud.getNumeroIdentificacion(), registroa.getIdAportante(),
                        solicitud.getIndicePlanilla().getNombreArchivo());

                EstadoArchivoPorBloque estadoA = entityManager
                        .createNamedQuery(NamedQueriesConstants.BUSQUEDA_BLOQUE_INDICE, EstadoArchivoPorBloque.class)
                        .setParameter("idIndicePlanilla", solicitud.getIdPlanillaInformacion()).getSingleResult();

                registroa.setIdAportante("" + solicitud.getNumeroIdentificacion());
                solicitud.getIndicePlanilla().setNombreArchivo(cambioNombreArchivo);
                solicitud.getIndicePlanilla().setEstadoArchivo(EstadoProcesoArchivoEnum.APROBADO);
                estadoA.setEstadoBloque5(EstadoProcesoArchivoEnum.APROBADO);
                estadoA.setAccionBloque5(AccionProcesoArchivoEnum.REINTENTAR_BLOQUE);

                List<IndicePlanillaOF> indiceF = entityManager
                        .createNamedQuery(NamedQueriesConstants.CONSULTAR_ARCHIVOS_ASOCIADOS_FINANCIERO, IndicePlanillaOF.class)
                        .setParameter("numeroPlanilla", "" + solicitud.getIndicePlanilla().getIdPlanilla()).getResultList();

                indicesOI.add(solicitud.getIndicePlanilla());
                indicesOF.add(indiceF.get(0));
                entityManager.merge(solicitud);
                entityManager.merge(indiceI);
                entityManager.merge(solicitud.getIndicePlanilla());
                entityManager.merge(registroi);
                entityManager.merge(registroa);
                entityManager.merge(estadoI);
                entityManager.merge(estadoA);

            }

            if (solicitud.getIndicePlanilla().getTipoArchivo() == TipoArchivoPilaEnum.ARCHIVO_OI_IR) {

                PilaArchivoIRegistro1 registroir = entityManager
                        .createNamedQuery(NamedQueriesConstants.BUSQUEDA_INDICE_PLANILLA_I, PilaArchivoIRegistro1.class)
                        .setParameter("idIndicePlanilla", solicitud.getIndicePlanilla().getId()).getSingleResult();

                String cambioNombreArchivo = modificarNombreArchivo(solicitud.getNumeroIdentificacion(), registroir.getIdAportante(),
                        solicitud.getIndicePlanilla().getNombreArchivo());

                EstadoArchivoPorBloque estadoIR = entityManager
                        .createNamedQuery(NamedQueriesConstants.BUSQUEDA_BLOQUE_INDICE, EstadoArchivoPorBloque.class)
                        .setParameter("idIndicePlanilla", solicitud.getIndicePlanilla().getId()).getSingleResult();

                solicitud.getIndicePlanilla().setNombreArchivo(cambioNombreArchivo);
                solicitud.getIndicePlanilla().setEstadoArchivo(EstadoProcesoArchivoEnum.APROBADO);
                registroir.setIdAportante("" + solicitud.getNumeroIdentificacion());
                estadoIR.setEstadoBloque5(EstadoProcesoArchivoEnum.APROBADO);
                estadoIR.setAccionBloque5(AccionProcesoArchivoEnum.REINTENTAR_BLOQUE);

                PilaArchivoARegistro1 registroar = entityManager
                        .createNamedQuery(NamedQueriesConstants.BUSQUEDA_INDICE_PLANILLA_A, PilaArchivoARegistro1.class)
                        .setParameter("idIndicePlanilla", solicitud.getIdPlanillaInformacion()).getSingleResult();

                IndicePlanilla indiceAR = entityManager
                        .createNamedQuery(NamedQueriesConstants.CONSULTAR_ARCHIVOS_ASOCIADOS_INFORMACION, IndicePlanilla.class)
                        .setParameter("numeroPlanilla", solicitud.getIndicePlanilla().getIdPlanilla())
                        .setParameter("tipoArchivo", TipoArchivoPilaEnum.ARCHIVO_OI_A).getSingleResult();

                cambioNombreArchivo = modificarNombreArchivo(solicitud.getNumeroIdentificacion(), registroar.getIdAportante(),
                        indiceAR.getNombreArchivo());

                EstadoArchivoPorBloque estadoAR = entityManager
                        .createNamedQuery(NamedQueriesConstants.BUSQUEDA_BLOQUE_INDICE, EstadoArchivoPorBloque.class)
                        .setParameter("idIndicePlanilla", solicitud.getIdPlanillaInformacion()).getSingleResult();

                registroar.setIdAportante("" + solicitud.getNumeroIdentificacion());
                indiceAR.setNombreArchivo(cambioNombreArchivo);
                indiceAR.setEstadoArchivo(EstadoProcesoArchivoEnum.APROBADO);
                estadoAR.setEstadoBloque5(EstadoProcesoArchivoEnum.APROBADO);
                estadoAR.setAccionBloque5(AccionProcesoArchivoEnum.REINTENTAR_BLOQUE);

                List<IndicePlanillaOF> indiceF = entityManager
                        .createNamedQuery(NamedQueriesConstants.CONSULTAR_ARCHIVOS_ASOCIADOS_FINANCIERO, IndicePlanillaOF.class)
                        .setParameter("numeroPlanilla", "" + solicitud.getIndicePlanilla().getIdPlanilla()).getResultList();

                indicesOI.add(solicitud.getIndicePlanilla());
                indicesOF.add(indiceF.get(0));
                entityManager.merge(solicitud);
                entityManager.merge(solicitud.getIndicePlanilla());
                entityManager.merge(indiceAR);
                entityManager.merge(registroir);
                entityManager.merge(registroar);
                entityManager.merge(estadoIR);
                entityManager.merge(estadoAR);
            }

            if (solicitud.getIndicePlanilla().getTipoArchivo() == TipoArchivoPilaEnum.ARCHIVO_OI_AR) {

                PilaArchivoIRegistro1 registroir = entityManager
                        .createNamedQuery(NamedQueriesConstants.BUSQUEDA_INDICE_PLANILLA_I, PilaArchivoIRegistro1.class)
                        .setParameter("idIndicePlanilla", solicitud.getIndicePlanilla().getId()).getSingleResult();

                IndicePlanilla indiceIR = entityManager
                        .createNamedQuery(NamedQueriesConstants.CONSULTAR_ARCHIVOS_ASOCIADOS_INFORMACION, IndicePlanilla.class)
                        .setParameter("numeroPlanilla", solicitud.getIndicePlanilla().getIdPlanilla())
                        .setParameter("tipoArchivo", TipoArchivoPilaEnum.ARCHIVO_OI_I).getSingleResult();

                String cambioNombreArchivo = modificarNombreArchivo(solicitud.getNumeroIdentificacion(), registroir.getIdAportante(),
                        indiceIR.getNombreArchivo());

                EstadoArchivoPorBloque estadoIR = entityManager
                        .createNamedQuery(NamedQueriesConstants.BUSQUEDA_BLOQUE_INDICE, EstadoArchivoPorBloque.class)
                        .setParameter("idIndicePlanilla", solicitud.getIndicePlanilla().getId()).getSingleResult();

                indiceIR.setNombreArchivo(cambioNombreArchivo);
                indiceIR.setEstadoArchivo(EstadoProcesoArchivoEnum.APROBADO);
                registroir.setIdAportante("" + solicitud.getNumeroIdentificacion());
                estadoIR.setEstadoBloque5(EstadoProcesoArchivoEnum.APROBADO);
                estadoIR.setAccionBloque5(AccionProcesoArchivoEnum.REINTENTAR_BLOQUE);

                PilaArchivoARegistro1 registroar = entityManager
                        .createNamedQuery(NamedQueriesConstants.BUSQUEDA_INDICE_PLANILLA_A, PilaArchivoARegistro1.class)
                        .setParameter("idIndicePlanilla", solicitud.getIdPlanillaInformacion()).getSingleResult();

                cambioNombreArchivo = modificarNombreArchivo(solicitud.getNumeroIdentificacion(), registroar.getIdAportante(),
                        solicitud.getIndicePlanilla().getNombreArchivo());

                EstadoArchivoPorBloque estadoAR = entityManager
                        .createNamedQuery(NamedQueriesConstants.BUSQUEDA_BLOQUE_INDICE, EstadoArchivoPorBloque.class)
                        .setParameter("idIndicePlanilla", solicitud.getIdPlanillaInformacion()).getSingleResult();

                registroar.setIdAportante("" + solicitud.getNumeroIdentificacion());
                solicitud.getIndicePlanilla().setNombreArchivo(cambioNombreArchivo);
                solicitud.getIndicePlanilla().setEstadoArchivo(EstadoProcesoArchivoEnum.APROBADO);
                estadoAR.setEstadoBloque5(EstadoProcesoArchivoEnum.APROBADO);
                estadoAR.setAccionBloque5(AccionProcesoArchivoEnum.REINTENTAR_BLOQUE);

                List<IndicePlanillaOF> indiceF = entityManager
                        .createNamedQuery(NamedQueriesConstants.CONSULTAR_ARCHIVOS_ASOCIADOS_FINANCIERO, IndicePlanillaOF.class)
                        .setParameter("numeroPlanilla", "" + solicitud.getIndicePlanilla().getIdPlanilla()).getResultList();

                indicesOI.add(solicitud.getIndicePlanilla());
                indicesOF.add(indiceF.get(0));
                entityManager.merge(solicitud);
                entityManager.merge(indiceIR);
                entityManager.merge(solicitud.getIndicePlanilla());
                entityManager.merge(registroir);
                entityManager.merge(registroar);
                entityManager.merge(estadoIR);
                entityManager.merge(estadoAR);
            }

            if (solicitud.getIndicePlanilla().getTipoArchivo() == TipoArchivoPilaEnum.ARCHIVO_OI_IP) {

                PilaArchivoIPRegistro1 registroip = entityManager
                        .createNamedQuery(NamedQueriesConstants.BUSQUEDA_INDICE_PLANILLA_IP, PilaArchivoIPRegistro1.class)
                        .setParameter("idIndicePlanilla", solicitud.getIndicePlanilla().getId()).getSingleResult();

                String cambioNombreArchivo = modificarNombreArchivo(solicitud.getNumeroIdentificacion(), registroip.getIdPagador(),
                        solicitud.getIndicePlanilla().getNombreArchivo());

                EstadoArchivoPorBloque estadoIP = entityManager
                        .createNamedQuery(NamedQueriesConstants.BUSQUEDA_BLOQUE_INDICE, EstadoArchivoPorBloque.class)
                        .setParameter("idIndicePlanilla", solicitud.getIndicePlanilla().getId()).getSingleResult();

                solicitud.getIndicePlanilla().setNombreArchivo(cambioNombreArchivo);
                solicitud.getIndicePlanilla().setEstadoArchivo(EstadoProcesoArchivoEnum.APROBADO);
                registroip.setIdPagador("" + solicitud.getNumeroIdentificacion());
                estadoIP.setEstadoBloque5(EstadoProcesoArchivoEnum.APROBADO);
                estadoIP.setAccionBloque5(AccionProcesoArchivoEnum.REINTENTAR_BLOQUE);

                PilaArchivoAPRegistro1 registroap = entityManager
                        .createNamedQuery(NamedQueriesConstants.BUSQUEDA_INDICE_PLANILLA_AP, PilaArchivoAPRegistro1.class)
                        .setParameter("idIndicePlanilla", solicitud.getIdPlanillaInformacion()).getSingleResult();

                IndicePlanilla indiceAP = entityManager
                        .createNamedQuery(NamedQueriesConstants.CONSULTAR_ARCHIVOS_ASOCIADOS_INFORMACION, IndicePlanilla.class)
                        .setParameter("numeroPlanilla", solicitud.getIndicePlanilla().getIdPlanilla())
                        .setParameter("tipoArchivo", TipoArchivoPilaEnum.ARCHIVO_OI_AP).getSingleResult();

                cambioNombreArchivo = modificarNombreArchivo(solicitud.getNumeroIdentificacion(), registroap.getIdPagador(),
                        indiceAP.getNombreArchivo());

                EstadoArchivoPorBloque estadoAP = entityManager
                        .createNamedQuery(NamedQueriesConstants.BUSQUEDA_BLOQUE_INDICE, EstadoArchivoPorBloque.class)
                        .setParameter("idIndicePlanilla", solicitud.getIdPlanillaInformacion()).getSingleResult();

                registroap.setIdPagador("" + solicitud.getNumeroIdentificacion());
                indiceAP.setNombreArchivo(cambioNombreArchivo);
                indiceAP.setEstadoArchivo(EstadoProcesoArchivoEnum.APROBADO);
                estadoAP.setEstadoBloque5(EstadoProcesoArchivoEnum.APROBADO);
                estadoAP.setAccionBloque5(AccionProcesoArchivoEnum.REINTENTAR_BLOQUE);

                List<IndicePlanillaOF> indiceF = entityManager
                        .createNamedQuery(NamedQueriesConstants.CONSULTAR_ARCHIVOS_ASOCIADOS_FINANCIERO, IndicePlanillaOF.class)
                        .setParameter("numeroPlanilla", "" + solicitud.getIndicePlanilla().getIdPlanilla()).getResultList();

                indicesOI.add(solicitud.getIndicePlanilla());
                indicesOF.add(indiceF.get(0));
                entityManager.merge(solicitud);
                entityManager.merge(solicitud.getIndicePlanilla());
                entityManager.merge(indiceAP);
                entityManager.merge(registroip);
                entityManager.merge(registroap);
                entityManager.merge(estadoIP);
                entityManager.merge(estadoAP);

            }

            if (solicitud.getIndicePlanilla().getTipoArchivo() == TipoArchivoPilaEnum.ARCHIVO_OI_AP) {

                PilaArchivoIPRegistro1 registroip = entityManager
                        .createNamedQuery(NamedQueriesConstants.BUSQUEDA_INDICE_PLANILLA_IP, PilaArchivoIPRegistro1.class)
                        .setParameter("idIndicePlanilla", solicitud.getIndicePlanilla().getId()).getSingleResult();

                IndicePlanilla indiceIP = entityManager
                        .createNamedQuery(NamedQueriesConstants.CONSULTAR_ARCHIVOS_ASOCIADOS_INFORMACION, IndicePlanilla.class)
                        .setParameter("numeroPlanilla", solicitud.getIndicePlanilla().getIdPlanilla())
                        .setParameter("tipoArchivo", TipoArchivoPilaEnum.ARCHIVO_OI_IP).getSingleResult();

                String cambioNombreArchivo = modificarNombreArchivo(solicitud.getNumeroIdentificacion(), registroip.getIdPagador(),
                        indiceIP.getNombreArchivo());

                EstadoArchivoPorBloque estadoIP = entityManager
                        .createNamedQuery(NamedQueriesConstants.BUSQUEDA_BLOQUE_INDICE, EstadoArchivoPorBloque.class)
                        .setParameter("idIndicePlanilla", solicitud.getIndicePlanilla().getId()).getSingleResult();

                indiceIP.setNombreArchivo(cambioNombreArchivo);
                indiceIP.setEstadoArchivo(EstadoProcesoArchivoEnum.APROBADO);
                registroip.setIdPagador("" + solicitud.getNumeroIdentificacion());
                estadoIP.setEstadoBloque5(EstadoProcesoArchivoEnum.APROBADO);
                estadoIP.setAccionBloque5(AccionProcesoArchivoEnum.REINTENTAR_BLOQUE);

                PilaArchivoAPRegistro1 registroap = entityManager
                        .createNamedQuery(NamedQueriesConstants.BUSQUEDA_INDICE_PLANILLA_AP, PilaArchivoAPRegistro1.class)
                        .setParameter("idIndicePlanilla", solicitud.getIdPlanillaInformacion()).getSingleResult();

                cambioNombreArchivo = modificarNombreArchivo(solicitud.getNumeroIdentificacion(), registroap.getIdPagador(),
                        solicitud.getIndicePlanilla().getNombreArchivo());

                EstadoArchivoPorBloque estadoAP = entityManager
                        .createNamedQuery(NamedQueriesConstants.BUSQUEDA_BLOQUE_INDICE, EstadoArchivoPorBloque.class)
                        .setParameter("idIndicePlanilla", solicitud.getIdPlanillaInformacion()).getSingleResult();

                registroap.setIdPagador("" + solicitud.getNumeroIdentificacion());
                solicitud.getIndicePlanilla().setNombreArchivo(cambioNombreArchivo);
                solicitud.getIndicePlanilla().setEstadoArchivo(EstadoProcesoArchivoEnum.APROBADO);
                estadoAP.setEstadoBloque5(EstadoProcesoArchivoEnum.APROBADO);
                estadoAP.setAccionBloque5(AccionProcesoArchivoEnum.REINTENTAR_BLOQUE);

                List<IndicePlanillaOF> indiceF = entityManager
                        .createNamedQuery(NamedQueriesConstants.CONSULTAR_ARCHIVOS_ASOCIADOS_FINANCIERO, IndicePlanillaOF.class)
                        .setParameter("numeroPlanilla", "" + solicitud.getIndicePlanilla().getIdPlanilla()).getResultList();

                indicesOI.add(solicitud.getIndicePlanilla());
                indicesOF.add(indiceF.get(0));
                entityManager.merge(solicitud);
                entityManager.merge(indiceIP);
                entityManager.merge(solicitud.getIndicePlanilla());
                entityManager.merge(registroip);
                entityManager.merge(registroap);
                entityManager.merge(estadoIP);
                entityManager.merge(estadoAP);
            }

            if (solicitud.getIndicePlanilla().getTipoArchivo() == TipoArchivoPilaEnum.ARCHIVO_OI_IPR) {

                PilaArchivoIPRegistro1 registroipr = entityManager
                        .createNamedQuery(NamedQueriesConstants.BUSQUEDA_INDICE_PLANILLA_IP, PilaArchivoIPRegistro1.class)
                        .setParameter("idIndicePlanilla", solicitud.getIndicePlanilla().getId()).getSingleResult();

                String cambioNombreArchivo = modificarNombreArchivo(solicitud.getNumeroIdentificacion(), registroipr.getIdPagador(),
                        solicitud.getIndicePlanilla().getNombreArchivo());

                EstadoArchivoPorBloque estadoIPR = entityManager
                        .createNamedQuery(NamedQueriesConstants.BUSQUEDA_BLOQUE_INDICE, EstadoArchivoPorBloque.class)
                        .setParameter("idIndicePlanilla", solicitud.getIndicePlanilla().getId()).getSingleResult();

                solicitud.getIndicePlanilla().setNombreArchivo(cambioNombreArchivo);
                solicitud.getIndicePlanilla().setEstadoArchivo(EstadoProcesoArchivoEnum.APROBADO);
                registroipr.setIdPagador("" + solicitud.getNumeroIdentificacion());
                estadoIPR.setEstadoBloque5(EstadoProcesoArchivoEnum.APROBADO);
                estadoIPR.setAccionBloque5(AccionProcesoArchivoEnum.REINTENTAR_BLOQUE);

                PilaArchivoAPRegistro1 registroapr = entityManager
                        .createNamedQuery(NamedQueriesConstants.BUSQUEDA_INDICE_PLANILLA_AP, PilaArchivoAPRegistro1.class)
                        .setParameter("idIndicePlanilla", solicitud.getIdPlanillaInformacion()).getSingleResult();

                IndicePlanilla indiceAPR = entityManager
                        .createNamedQuery(NamedQueriesConstants.CONSULTAR_ARCHIVOS_ASOCIADOS_INFORMACION, IndicePlanilla.class)
                        .setParameter("numeroPlanilla", solicitud.getIndicePlanilla().getIdPlanilla())
                        .setParameter("tipoArchivo", TipoArchivoPilaEnum.ARCHIVO_OI_AP).getSingleResult();

                cambioNombreArchivo = modificarNombreArchivo(solicitud.getNumeroIdentificacion(), registroapr.getIdPagador(),
                        indiceAPR.getNombreArchivo());

                EstadoArchivoPorBloque estadoAPR = entityManager
                        .createNamedQuery(NamedQueriesConstants.BUSQUEDA_BLOQUE_INDICE, EstadoArchivoPorBloque.class)
                        .setParameter("idIndicePlanilla", solicitud.getIdPlanillaInformacion()).getSingleResult();

                registroapr.setIdPagador("" + solicitud.getNumeroIdentificacion());
                indiceAPR.setNombreArchivo(cambioNombreArchivo);
                indiceAPR.setEstadoArchivo(EstadoProcesoArchivoEnum.APROBADO);
                estadoAPR.setEstadoBloque5(EstadoProcesoArchivoEnum.APROBADO);
                estadoAPR.setAccionBloque5(AccionProcesoArchivoEnum.REINTENTAR_BLOQUE);

                List<IndicePlanillaOF> indiceF = entityManager
                        .createNamedQuery(NamedQueriesConstants.CONSULTAR_ARCHIVOS_ASOCIADOS_FINANCIERO, IndicePlanillaOF.class)
                        .setParameter("numeroPlanilla", "" + solicitud.getIndicePlanilla().getIdPlanilla()).getResultList();

                indicesOI.add(solicitud.getIndicePlanilla());
                indicesOF.add(indiceF.get(0));
                entityManager.merge(solicitud);
                entityManager.merge(solicitud.getIndicePlanilla());
                entityManager.merge(indiceAPR);
                entityManager.merge(registroipr);
                entityManager.merge(registroapr);
                entityManager.merge(estadoIPR);
                entityManager.merge(estadoAPR);
            }

            if (solicitud.getIndicePlanilla().getTipoArchivo() == TipoArchivoPilaEnum.ARCHIVO_OI_APR) {

                PilaArchivoIPRegistro1 registroipr = entityManager
                        .createNamedQuery(NamedQueriesConstants.BUSQUEDA_INDICE_PLANILLA_IP, PilaArchivoIPRegistro1.class)
                        .setParameter("idIndicePlanilla", solicitud.getIndicePlanilla().getId()).getSingleResult();

                IndicePlanilla indiceIPR = entityManager
                        .createNamedQuery(NamedQueriesConstants.CONSULTAR_ARCHIVOS_ASOCIADOS_INFORMACION, IndicePlanilla.class)
                        .setParameter("numeroPlanilla", solicitud.getIndicePlanilla().getIdPlanilla())
                        .setParameter("tipoArchivo", TipoArchivoPilaEnum.ARCHIVO_OI_IP).getSingleResult();

                String cambioNombreArchivo = modificarNombreArchivo(solicitud.getNumeroIdentificacion(), registroipr.getIdPagador(),
                        indiceIPR.getNombreArchivo());

                EstadoArchivoPorBloque estadoIPR = entityManager
                        .createNamedQuery(NamedQueriesConstants.BUSQUEDA_BLOQUE_INDICE, EstadoArchivoPorBloque.class)
                        .setParameter("idIndicePlanilla", solicitud.getIndicePlanilla().getId()).getSingleResult();

                indiceIPR.setNombreArchivo(cambioNombreArchivo);
                indiceIPR.setEstadoArchivo(EstadoProcesoArchivoEnum.APROBADO);
                registroipr.setIdPagador("" + solicitud.getNumeroIdentificacion());
                estadoIPR.setEstadoBloque5(EstadoProcesoArchivoEnum.APROBADO);
                estadoIPR.setAccionBloque5(AccionProcesoArchivoEnum.REINTENTAR_BLOQUE);

                PilaArchivoAPRegistro1 registroapr = entityManager
                        .createNamedQuery(NamedQueriesConstants.BUSQUEDA_INDICE_PLANILLA_AP, PilaArchivoAPRegistro1.class)
                        .setParameter("idIndicePlanilla", solicitud.getIdPlanillaInformacion()).getSingleResult();

                cambioNombreArchivo = modificarNombreArchivo(solicitud.getNumeroIdentificacion(), registroapr.getIdPagador(),
                        solicitud.getIndicePlanilla().getNombreArchivo());

                EstadoArchivoPorBloque estadoAPR = entityManager
                        .createNamedQuery(NamedQueriesConstants.BUSQUEDA_BLOQUE_INDICE, EstadoArchivoPorBloque.class)
                        .setParameter("idIndicePlanilla", solicitud.getIdPlanillaInformacion()).getSingleResult();

                registroapr.setIdPagador("" + solicitud.getNumeroIdentificacion());
                solicitud.getIndicePlanilla().setNombreArchivo(cambioNombreArchivo);
                solicitud.getIndicePlanilla().setEstadoArchivo(EstadoProcesoArchivoEnum.APROBADO);
                estadoAPR.setEstadoBloque5(EstadoProcesoArchivoEnum.APROBADO);
                estadoAPR.setAccionBloque5(AccionProcesoArchivoEnum.REINTENTAR_BLOQUE);

                List<IndicePlanillaOF> indiceF = entityManager
                        .createNamedQuery(NamedQueriesConstants.CONSULTAR_ARCHIVOS_ASOCIADOS_FINANCIERO, IndicePlanillaOF.class)
                        .setParameter("numeroPlanilla", "" + solicitud.getIndicePlanilla().getIdPlanilla()).getResultList();

                indicesOI.add(solicitud.getIndicePlanilla());
                indicesOF.add(indiceF.get(0));
                entityManager.merge(solicitud);
                entityManager.merge(indiceIPR);
                entityManager.merge(solicitud.getIndicePlanilla());
                entityManager.merge(registroipr);
                entityManager.merge(registroapr);
                entityManager.merge(estadoIPR);
                entityManager.merge(estadoAPR);
            }
        }

        result.setIndicesOI(indicesOI);
        result.setIndicesOF(indicesOF);
        return result;

    }

    /**
     * (non-Javadoc)
     * 
     * @see com.asopagos.bandejainconsistencias.service.PilaBandejaService#rechazarSolicitudCambioIden(java.util.List,
     *      com.asopagos.rest.security.dto.UserDTO,
     *      com.asopagos.enumeraciones.pila.RazonRechazoSolicitudCambioIdenEnum,
     *      java.lang.String)
     */
    @Override
    public void rechazarSolicitudCambioIden(List<IndiceCorreccionPlanilla> listaSolicitudes, UserDTO user,
            RazonRechazoSolicitudCambioIdenEnum razonRechazo, String comentarios) {

        try {
            logger.debug(
                    "Inicia rechazarSolicitudCambioIden(List<IndiceCorreccionPlanilla> listaSolicitudes, UserDTO user,RazonRechazoSolicitudCambioIdenEnum razonRechazo, String comentarios):Inicia Busqueda, recursos  encontrados");
            // se recorre la lista de los registros que el supervisor haya
            // decidido rechazar
            for (IndiceCorreccionPlanilla solicitud : listaSolicitudes) {
                solicitud.setAccionCorreccion(AccionCorreccionPilaEnum.ANULAR_SOLICITUD_CAMBIO_IDENTIFICACION);
                solicitud.setFechaRespuesta(new Date());
                solicitud.setUsuarioAprobador(user.getEmail());
                solicitud.setRazonRechazo(razonRechazo);
                solicitud.setComentarios(comentarios);
                // se establece que tipo de archivo fue el que inicio la
                // solicitud de cambio de identificacion
                if (solicitud.getIndicePlanilla().getTipoArchivo() == TipoArchivoPilaEnum.ARCHIVO_OI_I) {
                    try {
                        logger.debug(
                                "Inicia rechazarSolicitudCambioIden(List<IndiceCorreccionPlanilla> listaSolicitudes, UserDTO user,RazonRechazoSolicitudCambioIdenEnum razonRechazo,String comentarios):Inicia Busqueda por Archivo I");

                        PilaArchivoIRegistro1 registroi = entityManager
                                .createNamedQuery(NamedQueriesConstants.BUSQUEDA_INDICE_PLANILLA_I, PilaArchivoIRegistro1.class)
                                .setParameter("idIndicePlanilla", solicitud.getIndicePlanilla().getId()).getSingleResult();

                        EstadoArchivoPorBloque estadoI = entityManager
                                .createNamedQuery(NamedQueriesConstants.BUSQUEDA_BLOQUE_INDICE, EstadoArchivoPorBloque.class)
                                .setParameter("idIndicePlanilla", solicitud.getIndicePlanilla().getId()).getSingleResult();

                        solicitud.getIndicePlanilla().setEstadoArchivo(EstadoProcesoArchivoEnum.RECHAZADO);
                        estadoI.setEstadoBloque5(EstadoProcesoArchivoEnum.RECHAZADO);
                        estadoI.setAccionBloque5(AccionProcesoArchivoEnum.EJECUTAR_BLOQUE_5);

                        PilaArchivoARegistro1 registroa = entityManager
                                .createNamedQuery(NamedQueriesConstants.BUSQUEDA_INDICE_PLANILLA_A, PilaArchivoARegistro1.class)
                                .setParameter("idIndicePlanilla", solicitud.getIdPlanillaInformacion()).getSingleResult();

                        IndicePlanilla indiceA = entityManager
                                .createNamedQuery(NamedQueriesConstants.CONSULTAR_ARCHIVOS_ASOCIADOS_INFORMACION, IndicePlanilla.class)
                                .setParameter("numeroPlanilla", solicitud.getIndicePlanilla().getIdPlanilla())
                                .setParameter("tipoArchivo", TipoArchivoPilaEnum.ARCHIVO_OI_A).getSingleResult();

                        EstadoArchivoPorBloque estadoA = entityManager
                                .createNamedQuery(NamedQueriesConstants.BUSQUEDA_BLOQUE_INDICE, EstadoArchivoPorBloque.class)
                                .setParameter("idIndicePlanilla", solicitud.getIdPlanillaInformacion()).getSingleResult();

                        indiceA.setEstadoArchivo(EstadoProcesoArchivoEnum.RECHAZADO);
                        estadoA.setEstadoBloque5(EstadoProcesoArchivoEnum.RECHAZADO);
                        estadoA.setAccionBloque5(AccionProcesoArchivoEnum.EJECUTAR_BLOQUE_5);

                        entityManager.merge(solicitud);
                        entityManager.merge(solicitud.getIndicePlanilla());
                        entityManager.merge(indiceA);
                        entityManager.merge(registroi);
                        entityManager.merge(registroa);
                        entityManager.merge(estadoI);
                        entityManager.merge(estadoA);

                    } catch (Exception e) {
                        logger.error("Error al realizar la consulta,verifique los datos", e);
                        logger.debug("Finaliza enviarSolicitudCambioIden(BandejaSolicitudCambioIden, solicitudCambio)");
                        throw new TechnicalException(MensajesGeneralConstants.ERROR_TECNICO_INESPERADO);
                    }
                }

                if (solicitud.getIndicePlanilla().getTipoArchivo() == TipoArchivoPilaEnum.ARCHIVO_OI_A) {
                    try {
                        logger.debug(
                                "Inicia rechazarSolicitudCambioIden(List<IndiceCorreccionPlanilla> listaSolicitudes, UserDTO user,RazonRechazoSolicitudCambioIdenEnum razonRechazo,String comentarios):Inicia Busqueda por archivo A");

                        PilaArchivoIRegistro1 registroi = entityManager
                                .createNamedQuery(NamedQueriesConstants.BUSQUEDA_INDICE_PLANILLA_I, PilaArchivoIRegistro1.class)
                                .setParameter("idIndicePlanilla", solicitud.getIndicePlanilla().getId()).getSingleResult();

                        IndicePlanilla indiceI = entityManager
                                .createNamedQuery(NamedQueriesConstants.CONSULTAR_ARCHIVOS_ASOCIADOS_INFORMACION, IndicePlanilla.class)
                                .setParameter("numeroPlanilla", solicitud.getIndicePlanilla().getIdPlanilla())
                                .setParameter("tipoArchivo", TipoArchivoPilaEnum.ARCHIVO_OI_I).getSingleResult();

                        EstadoArchivoPorBloque estadoI = entityManager
                                .createNamedQuery(NamedQueriesConstants.BUSQUEDA_BLOQUE_INDICE, EstadoArchivoPorBloque.class)
                                .setParameter("idIndicePlanilla", solicitud.getIndicePlanilla().getId()).getSingleResult();

                        indiceI.setEstadoArchivo(EstadoProcesoArchivoEnum.RECHAZADO);
                        estadoI.setEstadoBloque5(EstadoProcesoArchivoEnum.RECHAZADO);
                        estadoI.setAccionBloque5(AccionProcesoArchivoEnum.EJECUTAR_BLOQUE_5);

                        PilaArchivoARegistro1 registroa = entityManager
                                .createNamedQuery(NamedQueriesConstants.BUSQUEDA_INDICE_PLANILLA_A, PilaArchivoARegistro1.class)
                                .setParameter("idIndicePlanilla", solicitud.getIdPlanillaInformacion()).getSingleResult();

                        EstadoArchivoPorBloque estadoA = entityManager
                                .createNamedQuery(NamedQueriesConstants.BUSQUEDA_BLOQUE_INDICE, EstadoArchivoPorBloque.class)
                                .setParameter("idIndicePlanilla", solicitud.getIdPlanillaInformacion()).getSingleResult();

                        solicitud.getIndicePlanilla().setEstadoArchivo(EstadoProcesoArchivoEnum.RECHAZADO);
                        estadoA.setEstadoBloque5(EstadoProcesoArchivoEnum.RECHAZADO);
                        estadoA.setAccionBloque5(AccionProcesoArchivoEnum.EJECUTAR_BLOQUE_5);

                        entityManager.merge(solicitud);
                        entityManager.merge(indiceI);
                        entityManager.merge(solicitud.getIndicePlanilla());
                        entityManager.merge(registroi);
                        entityManager.merge(registroa);
                        entityManager.merge(estadoI);
                        entityManager.merge(estadoA);

                    } catch (Exception e) {
                        logger.error("Error al realizar la consulta,verifique los datos", e);
                        logger.debug("Finaliza enviarSolicitudCambioIden(BandejaSolicitudCambioIden, solicitudCambio)");
                        throw new TechnicalException(MensajesGeneralConstants.ERROR_TECNICO_INESPERADO);
                    }
                }

                if (solicitud.getIndicePlanilla().getTipoArchivo() == TipoArchivoPilaEnum.ARCHIVO_OI_IR) {
                    try {
                        logger.debug(
                                "Inicia rechazarSolicitudCambioIden(List<IndiceCorreccionPlanilla> listaSolicitudes, UserDTO user,RazonRechazoSolicitudCambioIdenEnum razonRechazo,String comentarios):Inicia Busqueda por archivo IR ");

                        PilaArchivoIRegistro1 registroir = entityManager
                                .createNamedQuery(NamedQueriesConstants.BUSQUEDA_INDICE_PLANILLA_I, PilaArchivoIRegistro1.class)
                                .setParameter("idIndicePlanilla", solicitud.getIndicePlanilla().getId()).getSingleResult();

                        EstadoArchivoPorBloque estadoIR = entityManager
                                .createNamedQuery(NamedQueriesConstants.BUSQUEDA_BLOQUE_INDICE, EstadoArchivoPorBloque.class)
                                .setParameter("idIndicePlanilla", solicitud.getIndicePlanilla().getId()).getSingleResult();

                        solicitud.getIndicePlanilla().setEstadoArchivo(EstadoProcesoArchivoEnum.RECHAZADO);
                        estadoIR.setEstadoBloque5(EstadoProcesoArchivoEnum.RECHAZADO);
                        estadoIR.setAccionBloque5(AccionProcesoArchivoEnum.EJECUTAR_BLOQUE_5);

                        PilaArchivoARegistro1 registroar = entityManager
                                .createNamedQuery(NamedQueriesConstants.BUSQUEDA_INDICE_PLANILLA_A, PilaArchivoARegistro1.class)
                                .setParameter("idIndicePlanilla", solicitud.getIdPlanillaInformacion()).getSingleResult();

                        IndicePlanilla indiceAR = entityManager
                                .createNamedQuery(NamedQueriesConstants.CONSULTAR_ARCHIVOS_ASOCIADOS_INFORMACION, IndicePlanilla.class)
                                .setParameter("numeroPlanilla", solicitud.getIndicePlanilla().getIdPlanilla())
                                .setParameter("tipoArchivo", TipoArchivoPilaEnum.ARCHIVO_OI_A).getSingleResult();

                        EstadoArchivoPorBloque estadoAR = entityManager
                                .createNamedQuery(NamedQueriesConstants.BUSQUEDA_BLOQUE_INDICE, EstadoArchivoPorBloque.class)
                                .setParameter("idIndicePlanilla", solicitud.getIdPlanillaInformacion()).getSingleResult();

                        indiceAR.setEstadoArchivo(EstadoProcesoArchivoEnum.RECHAZADO);
                        estadoAR.setEstadoBloque5(EstadoProcesoArchivoEnum.RECHAZADO);
                        estadoAR.setAccionBloque5(AccionProcesoArchivoEnum.EJECUTAR_BLOQUE_5);

                        entityManager.merge(solicitud);
                        entityManager.merge(solicitud.getIndicePlanilla());
                        entityManager.merge(indiceAR);
                        entityManager.merge(registroir);
                        entityManager.merge(registroar);
                        entityManager.merge(estadoIR);
                        entityManager.merge(estadoAR);

                    } catch (Exception e) {
                        logger.error("Error al realizar la consulta,verifique los datos", e);
                        logger.debug("Finaliza enviarSolicitudCambioIden(BandejaSolicitudCambioIden, solicitudCambio)");
                        throw new TechnicalException(MensajesGeneralConstants.ERROR_TECNICO_INESPERADO);
                    }
                }

                if (solicitud.getIndicePlanilla().getTipoArchivo() == TipoArchivoPilaEnum.ARCHIVO_OI_AR) {
                    try {
                        logger.debug(
                                "Inicia rechazarSolicitudCambioIden(List<IndiceCorreccionPlanilla> listaSolicitudes, UserDTO user,RazonRechazoSolicitudCambioIdenEnum razonRechazo,String comentarios):Inicia Busqueda por archivo AR ");

                        PilaArchivoIRegistro1 registroir = entityManager
                                .createNamedQuery(NamedQueriesConstants.BUSQUEDA_INDICE_PLANILLA_I, PilaArchivoIRegistro1.class)
                                .setParameter("idIndicePlanilla", solicitud.getIndicePlanilla().getId()).getSingleResult();

                        IndicePlanilla indiceIR = entityManager
                                .createNamedQuery(NamedQueriesConstants.CONSULTAR_ARCHIVOS_ASOCIADOS_INFORMACION, IndicePlanilla.class)
                                .setParameter("numeroPlanilla", solicitud.getIndicePlanilla().getIdPlanilla())
                                .setParameter("tipoArchivo", TipoArchivoPilaEnum.ARCHIVO_OI_I).getSingleResult();

                        EstadoArchivoPorBloque estadoIR = entityManager
                                .createNamedQuery(NamedQueriesConstants.BUSQUEDA_BLOQUE_INDICE, EstadoArchivoPorBloque.class)
                                .setParameter("idIndicePlanilla", solicitud.getIndicePlanilla().getId()).getSingleResult();

                        indiceIR.setEstadoArchivo(EstadoProcesoArchivoEnum.RECHAZADO);
                        estadoIR.setEstadoBloque5(EstadoProcesoArchivoEnum.RECHAZADO);
                        estadoIR.setAccionBloque5(AccionProcesoArchivoEnum.EJECUTAR_BLOQUE_5);

                        PilaArchivoARegistro1 registroar = entityManager
                                .createNamedQuery(NamedQueriesConstants.BUSQUEDA_INDICE_PLANILLA_A, PilaArchivoARegistro1.class)
                                .setParameter("idIndicePlanilla", solicitud.getIdPlanillaInformacion()).getSingleResult();

                        EstadoArchivoPorBloque estadoAR = entityManager
                                .createNamedQuery(NamedQueriesConstants.BUSQUEDA_BLOQUE_INDICE, EstadoArchivoPorBloque.class)
                                .setParameter("idIndicePlanilla", solicitud.getIdPlanillaInformacion()).getSingleResult();

                        solicitud.getIndicePlanilla().setEstadoArchivo(EstadoProcesoArchivoEnum.RECHAZADO);
                        estadoAR.setEstadoBloque5(EstadoProcesoArchivoEnum.RECHAZADO);
                        estadoAR.setAccionBloque5(AccionProcesoArchivoEnum.EJECUTAR_BLOQUE_5);

                        entityManager.merge(solicitud);
                        entityManager.merge(indiceIR);
                        entityManager.merge(solicitud.getIndicePlanilla());
                        entityManager.merge(registroir);
                        entityManager.merge(registroar);
                        entityManager.merge(estadoIR);
                        entityManager.merge(estadoAR);

                    } catch (Exception e) {
                        logger.error("Error al realizar la consulta,verifique los datos", e);
                        logger.debug("Finaliza enviarSolicitudCambioIden(BandejaSolicitudCambioIden, solicitudCambio)");
                        throw new TechnicalException(MensajesGeneralConstants.ERROR_TECNICO_INESPERADO);
                    }
                }

                if (solicitud.getIndicePlanilla().getTipoArchivo() == TipoArchivoPilaEnum.ARCHIVO_OI_IP) {
                    try {
                        logger.debug(
                                "Inicia rechazarSolicitudCambioIden(List<IndiceCorreccionPlanilla> listaSolicitudes, UserDTO user,RazonRechazoSolicitudCambioIdenEnum razonRechazo,String comentarios):Inicia Busqueda por archivo IP ");

                        PilaArchivoIPRegistro1 registroip = entityManager
                                .createNamedQuery(NamedQueriesConstants.BUSQUEDA_INDICE_PLANILLA_IP, PilaArchivoIPRegistro1.class)
                                .setParameter("idIndicePlanilla", solicitud.getIndicePlanilla().getId()).getSingleResult();

                        EstadoArchivoPorBloque estadoIP = entityManager
                                .createNamedQuery(NamedQueriesConstants.BUSQUEDA_BLOQUE_INDICE, EstadoArchivoPorBloque.class)
                                .setParameter("idIndicePlanilla", solicitud.getIndicePlanilla().getId()).getSingleResult();

                        solicitud.getIndicePlanilla().setEstadoArchivo(EstadoProcesoArchivoEnum.RECHAZADO);
                        estadoIP.setEstadoBloque5(EstadoProcesoArchivoEnum.RECHAZADO);
                        estadoIP.setAccionBloque5(AccionProcesoArchivoEnum.EJECUTAR_BLOQUE_5);

                        PilaArchivoAPRegistro1 registroap = entityManager
                                .createNamedQuery(NamedQueriesConstants.BUSQUEDA_INDICE_PLANILLA_AP, PilaArchivoAPRegistro1.class)
                                .setParameter("idIndicePlanilla", solicitud.getIdPlanillaInformacion()).getSingleResult();

                        IndicePlanilla indiceAP = entityManager
                                .createNamedQuery(NamedQueriesConstants.CONSULTAR_ARCHIVOS_ASOCIADOS_INFORMACION, IndicePlanilla.class)
                                .setParameter("numeroPlanilla", solicitud.getIndicePlanilla().getIdPlanilla())
                                .setParameter("tipoArchivo", TipoArchivoPilaEnum.ARCHIVO_OI_AP).getSingleResult();

                        EstadoArchivoPorBloque estadoAP = entityManager
                                .createNamedQuery(NamedQueriesConstants.BUSQUEDA_BLOQUE_INDICE, EstadoArchivoPorBloque.class)
                                .setParameter("idIndicePlanilla", solicitud.getIdPlanillaInformacion()).getSingleResult();

                        indiceAP.setEstadoArchivo(EstadoProcesoArchivoEnum.RECHAZADO);
                        estadoAP.setEstadoBloque5(EstadoProcesoArchivoEnum.RECHAZADO);
                        estadoAP.setAccionBloque5(AccionProcesoArchivoEnum.EJECUTAR_BLOQUE_5);

                        entityManager.merge(solicitud);
                        entityManager.merge(solicitud.getIndicePlanilla());
                        entityManager.merge(indiceAP);
                        entityManager.merge(registroip);
                        entityManager.merge(registroap);
                        entityManager.merge(estadoIP);
                        entityManager.merge(estadoAP);

                    } catch (Exception e) {
                        logger.error("Error al realizar la consulta,verifique los datos", e);
                        logger.debug("Finaliza enviarSolicitudCambioIden(BandejaSolicitudCambioIden, solicitudCambio)");
                        throw new TechnicalException(MensajesGeneralConstants.ERROR_TECNICO_INESPERADO);
                    }
                }

                if (solicitud.getIndicePlanilla().getTipoArchivo() == TipoArchivoPilaEnum.ARCHIVO_OI_AP) {
                    try {
                        logger.debug(
                                "Inicia rechazarSolicitudCambioIden(List<IndiceCorreccionPlanilla> listaSolicitudes, UserDTO user,RazonRechazoSolicitudCambioIdenEnum razonRechazo,String comentarios):Inicia Busqueda por archivo AP ");

                        PilaArchivoIPRegistro1 registroip = entityManager
                                .createNamedQuery(NamedQueriesConstants.BUSQUEDA_INDICE_PLANILLA_IP, PilaArchivoIPRegistro1.class)
                                .setParameter("idIndicePlanilla", solicitud.getIndicePlanilla().getId()).getSingleResult();

                        IndicePlanilla indiceIP = entityManager
                                .createNamedQuery(NamedQueriesConstants.CONSULTAR_ARCHIVOS_ASOCIADOS_INFORMACION, IndicePlanilla.class)
                                .setParameter("numeroPlanilla", solicitud.getIndicePlanilla().getIdPlanilla())
                                .setParameter("tipoArchivo", TipoArchivoPilaEnum.ARCHIVO_OI_IP).getSingleResult();

                        EstadoArchivoPorBloque estadoIP = entityManager
                                .createNamedQuery(NamedQueriesConstants.BUSQUEDA_BLOQUE_INDICE, EstadoArchivoPorBloque.class)
                                .setParameter("idIndicePlanilla", solicitud.getIndicePlanilla().getId()).getSingleResult();

                        indiceIP.setEstadoArchivo(EstadoProcesoArchivoEnum.RECHAZADO);
                        estadoIP.setEstadoBloque5(EstadoProcesoArchivoEnum.RECHAZADO);
                        estadoIP.setAccionBloque5(AccionProcesoArchivoEnum.EJECUTAR_BLOQUE_5);

                        PilaArchivoAPRegistro1 registroap = entityManager
                                .createNamedQuery(NamedQueriesConstants.BUSQUEDA_INDICE_PLANILLA_AP, PilaArchivoAPRegistro1.class)
                                .setParameter("idIndicePlanilla", solicitud.getIdPlanillaInformacion()).getSingleResult();

                        EstadoArchivoPorBloque estadoAP = entityManager
                                .createNamedQuery(NamedQueriesConstants.BUSQUEDA_BLOQUE_INDICE, EstadoArchivoPorBloque.class)
                                .setParameter("idIndicePlanilla", solicitud.getIdPlanillaInformacion()).getSingleResult();

                        solicitud.getIndicePlanilla().setEstadoArchivo(EstadoProcesoArchivoEnum.RECHAZADO);
                        estadoAP.setEstadoBloque5(EstadoProcesoArchivoEnum.RECHAZADO);
                        estadoAP.setAccionBloque5(AccionProcesoArchivoEnum.EJECUTAR_BLOQUE_5);

                        entityManager.merge(solicitud);
                        entityManager.merge(indiceIP);
                        entityManager.merge(solicitud.getIndicePlanilla());
                        entityManager.merge(registroip);
                        entityManager.merge(registroap);
                        entityManager.merge(estadoIP);
                        entityManager.merge(estadoAP);

                    } catch (Exception e) {
                        logger.error("Error al realizar la consulta,verifique los datos", e);
                        logger.debug("Finaliza enviarSolicitudCambioIden(BandejaSolicitudCambioIden, solicitudCambio)");
                        throw new TechnicalException(MensajesGeneralConstants.ERROR_TECNICO_INESPERADO);
                    }
                }

                if (solicitud.getIndicePlanilla().getTipoArchivo() == TipoArchivoPilaEnum.ARCHIVO_OI_IPR) {
                    try {
                        logger.debug(
                                "Inicia rechazarSolicitudCambioIden(List<IndiceCorreccionPlanilla> listaSolicitudes, UserDTO user,RazonRechazoSolicitudCambioIdenEnum razonRechazo,String comentarios):Inicia Busqueda por archivo IPR ");

                        PilaArchivoIPRegistro1 registroipr = entityManager
                                .createNamedQuery(NamedQueriesConstants.BUSQUEDA_INDICE_PLANILLA_IP, PilaArchivoIPRegistro1.class)
                                .setParameter("idIndicePlanilla", solicitud.getIndicePlanilla().getId()).getSingleResult();

                        EstadoArchivoPorBloque estadoIPR = entityManager
                                .createNamedQuery(NamedQueriesConstants.BUSQUEDA_BLOQUE_INDICE, EstadoArchivoPorBloque.class)
                                .setParameter("idIndicePlanilla", solicitud.getIndicePlanilla().getId()).getSingleResult();

                        solicitud.getIndicePlanilla().setEstadoArchivo(EstadoProcesoArchivoEnum.RECHAZADO);
                        estadoIPR.setEstadoBloque5(EstadoProcesoArchivoEnum.RECHAZADO);
                        estadoIPR.setAccionBloque5(AccionProcesoArchivoEnum.EJECUTAR_BLOQUE_5);

                        PilaArchivoAPRegistro1 registroapr = entityManager
                                .createNamedQuery(NamedQueriesConstants.BUSQUEDA_INDICE_PLANILLA_AP, PilaArchivoAPRegistro1.class)
                                .setParameter("idIndicePlanilla", solicitud.getIdPlanillaInformacion()).getSingleResult();

                        IndicePlanilla indiceAPR = entityManager
                                .createNamedQuery(NamedQueriesConstants.CONSULTAR_ARCHIVOS_ASOCIADOS_INFORMACION, IndicePlanilla.class)
                                .setParameter("numeroPlanilla", solicitud.getIndicePlanilla().getIdPlanilla())
                                .setParameter("tipoArchivo", TipoArchivoPilaEnum.ARCHIVO_OI_AP).getSingleResult();

                        EstadoArchivoPorBloque estadoAPR = entityManager
                                .createNamedQuery(NamedQueriesConstants.BUSQUEDA_BLOQUE_INDICE, EstadoArchivoPorBloque.class)
                                .setParameter("idIndicePlanilla", solicitud.getIdPlanillaInformacion()).getSingleResult();

                        indiceAPR.setEstadoArchivo(EstadoProcesoArchivoEnum.RECHAZADO);
                        estadoAPR.setEstadoBloque5(EstadoProcesoArchivoEnum.RECHAZADO);
                        estadoAPR.setAccionBloque5(AccionProcesoArchivoEnum.EJECUTAR_BLOQUE_5);

                        entityManager.merge(solicitud);
                        entityManager.merge(solicitud.getIndicePlanilla());
                        entityManager.merge(indiceAPR);
                        entityManager.merge(registroipr);
                        entityManager.merge(registroapr);
                        entityManager.merge(estadoIPR);
                        entityManager.merge(estadoAPR);

                    } catch (Exception e) {
                        logger.error("Error al realizar la consulta,verifique los datos", e);
                        logger.debug("Finaliza enviarSolicitudCambioIden(BandejaSolicitudCambioIden, solicitudCambio)");
                        throw new TechnicalException(MensajesGeneralConstants.ERROR_TECNICO_INESPERADO);
                    }
                }

                if (solicitud.getIndicePlanilla().getTipoArchivo() == TipoArchivoPilaEnum.ARCHIVO_OI_APR) {
                    try {
                        logger.debug(
                                "Inicia rechazarSolicitudCambioIden(List<IndiceCorreccionPlanilla> listaSolicitudes, UserDTO user,RazonRechazoSolicitudCambioIdenEnum razonRechazo,String comentarios):Inicia Busqueda por archivo APR ");

                        PilaArchivoIPRegistro1 registroipr = entityManager
                                .createNamedQuery(NamedQueriesConstants.BUSQUEDA_INDICE_PLANILLA_IP, PilaArchivoIPRegistro1.class)
                                .setParameter("idIndicePlanilla", solicitud.getIndicePlanilla().getId()).getSingleResult();

                        IndicePlanilla indiceIPR = entityManager
                                .createNamedQuery(NamedQueriesConstants.CONSULTAR_ARCHIVOS_ASOCIADOS_INFORMACION, IndicePlanilla.class)
                                .setParameter("numeroPlanilla", solicitud.getIndicePlanilla().getIdPlanilla())
                                .setParameter("tipoArchivo", TipoArchivoPilaEnum.ARCHIVO_OI_IP).getSingleResult();

                        EstadoArchivoPorBloque estadoIPR = entityManager
                                .createNamedQuery(NamedQueriesConstants.BUSQUEDA_BLOQUE_INDICE, EstadoArchivoPorBloque.class)
                                .setParameter("idIndicePlanilla", solicitud.getIndicePlanilla().getId()).getSingleResult();

                        indiceIPR.setEstadoArchivo(EstadoProcesoArchivoEnum.RECHAZADO);
                        estadoIPR.setEstadoBloque5(EstadoProcesoArchivoEnum.RECHAZADO);
                        estadoIPR.setAccionBloque5(AccionProcesoArchivoEnum.EJECUTAR_BLOQUE_5);

                        PilaArchivoAPRegistro1 registroapr = entityManager
                                .createNamedQuery(NamedQueriesConstants.BUSQUEDA_INDICE_PLANILLA_AP, PilaArchivoAPRegistro1.class)
                                .setParameter("idIndicePlanilla", solicitud.getIdPlanillaInformacion()).getSingleResult();

                        EstadoArchivoPorBloque estadoAPR = entityManager
                                .createNamedQuery(NamedQueriesConstants.BUSQUEDA_BLOQUE_INDICE, EstadoArchivoPorBloque.class)
                                .setParameter("idIndicePlanilla", solicitud.getIdPlanillaInformacion()).getSingleResult();

                        solicitud.getIndicePlanilla().setEstadoArchivo(EstadoProcesoArchivoEnum.RECHAZADO);
                        estadoAPR.setEstadoBloque5(EstadoProcesoArchivoEnum.RECHAZADO);
                        estadoAPR.setAccionBloque5(AccionProcesoArchivoEnum.EJECUTAR_BLOQUE_5);

                        entityManager.merge(solicitud);
                        entityManager.merge(indiceIPR);
                        entityManager.merge(solicitud.getIndicePlanilla());
                        entityManager.merge(registroipr);
                        entityManager.merge(registroapr);
                        entityManager.merge(estadoIPR);
                        entityManager.merge(estadoAPR);

                    } catch (Exception e) {
                        logger.error("Error al realizar la consulta,verifique los datos", e);
                        logger.debug("Finaliza enviarSolicitudCambioIden(BandejaSolicitudCambioIden, solicitudCambio)");
                        throw new TechnicalException(MensajesGeneralConstants.ERROR_TECNICO_INESPERADO);
                    }
                }
            }
        } catch (Exception localException2) {
        }

    }

    /**
     * (non-Javadoc)
     * 
     * @see com.asopagos.bandejainconsistencias.service.PilaBandejaService#busquedaSolicitudCambioIden(java.lang.Long,
     *      java.lang.Long, java.lang.Long)
     */
    @Override
    public List<IndiceCorreccionPlanilla> busquedaSolicitudCambioIden(Long numeroPlanilla, Long fechaInicio, Long fechaFin) {

        logger.debug("Inicia busquedaSolicitudCambioIden(Long numeroPlanilla, Long fechaInicio,Long fechaFin");
        List<IndiceCorreccionPlanilla> result = new ArrayList<IndiceCorreccionPlanilla>();
        // se establecen las fechas en formato Date para las consultas
        if ((fechaInicio != null) && (fechaFin != null)) {
            fechaI = new Date(fechaInicio);
            fechaF = new Date(fechaFin);
        }
        try {

            if (fechaFin != null && fechaInicio != null) {
                if (numeroPlanilla != null) {
                    // consulta por los 3 valores
                    result = entityManager
                            .createNamedQuery(NamedQueriesConstants.BUSQUEDA_SOLICITUD_FECHA_INICIO_FIN_PLANILLA,
                                    IndiceCorreccionPlanilla.class)
                            .setParameter("numeroPlanilla", numeroPlanilla).setParameter("fechaInicio", CalendarUtils.truncarHora(fechaI))
                            .setParameter("fechaFin", CalendarUtils.truncarHoraMaxima(fechaF)).getResultList();
                    return filtrarResultadoConsultaSolicitudes(evaluarResultadoSolicitudes(result));
                }
                else {
                    // consulta solo por fechas
                    result = entityManager
                            .createNamedQuery(NamedQueriesConstants.BUSQUEDA_SOLICITUD_FECHA_INICIO_FIN, IndiceCorreccionPlanilla.class)
                            .setParameter("fechaInicio", CalendarUtils.truncarHora(fechaI))
                            .setParameter("fechaFin", CalendarUtils.truncarHoraMaxima(fechaF)).getResultList();
                    return filtrarResultadoConsultaSolicitudes(evaluarResultadoSolicitudes(result));
                }

            }
            if (fechaFin != null) {
                if (numeroPlanilla != null) {
                    result = entityManager
                            .createNamedQuery(NamedQueriesConstants.BUSQUEDA_SOLICITUD_FECHA_FIN_PLANILLA, IndiceCorreccionPlanilla.class)
                            .setParameter("numeroPlanilla", numeroPlanilla)
                            .setParameter("fechaFin", CalendarUtils.truncarHoraMaxima(fechaF)).getResultList();
                    return filtrarResultadoConsultaSolicitudes(evaluarResultadoSolicitudes(result));
                }
                else {
                    result = entityManager
                            .createNamedQuery(NamedQueriesConstants.BUSQUEDA_SOLICITUD_FECHA_FIN, IndiceCorreccionPlanilla.class)
                            .setParameter("fechaFin", CalendarUtils.truncarHoraMaxima(fechaF)).getResultList();
                    return filtrarResultadoConsultaSolicitudes(evaluarResultadoSolicitudes(result));
                }

            }
            if (numeroPlanilla != null) {
                result = entityManager.createNamedQuery(NamedQueriesConstants.BUSQUEDA_SOLICITUD_PLANILLA, IndiceCorreccionPlanilla.class)
                        .setParameter("numeroPlanilla", numeroPlanilla).getResultList();
                return filtrarResultadoConsultaSolicitudes(evaluarResultadoSolicitudes(result));
            }

        } catch (Exception e) {
            logger.error("Error al realizar la consulta,verifique los datos", e);
            logger.debug("Finaliza busquedaSolicitudCambioIden(Long numeroPlanilla, Long fechaInicio,Long fechaFin");
            throw new TechnicalException(MensajesGeneralConstants.ERROR_TECNICO_INESPERADO);
        }
        logger.debug(
                "Finaliza busquedaSolicitudCambioIden(Long numeroPlanilla, Long fechaInicio,Long fechaFin) : no se encontro una busqueda compatible ");
        return null;

    }

    /**
     * (non-Javadoc)
     * 
     * @see com.asopagos.bandejainconsistencias.service.PilaBandejaService#crearAportante(java.lang.Long)
     */
    @Override
    public void crearAportante(Long idPlanilla) {
        // se establecen los datos de un aportante que aun no existe en la bd y
        // se valida a que registro estaba asociado
        try {
            logger.debug("Inicia crearAportante(Long idPlanilla): inicia la creacion del aportante");
            Empleador empleador = new Empleador();
            Empresa empresa = new Empresa();
            Persona persona = new Persona();
            Ubicacion ubicacion = new Ubicacion();
            Municipio municipio = new Municipio();

            PilaArchivoARegistro1 registroA = entityManager
                    .createNamedQuery(NamedQueriesConstants.BUSQUEDA_REGISTRO_A, PilaArchivoARegistro1.class)
                    .setParameter("idPlanilla", idPlanilla).getSingleResult();

            PilaArchivoAPRegistro1 registroAP = entityManager
                    .createNamedQuery(NamedQueriesConstants.BUSQUEDA_REGISTRO_AP, PilaArchivoAPRegistro1.class)
                    .setParameter("idPlanilla", idPlanilla).getSingleResult();

            if (registroA != null) {
                // se establecen todos los datos para la creacion del aportante
                empleador.setEstadoEmpleador(EstadoEmpleadorEnum.NO_FORMALIZADO_SIN_AFILIACION_CON_APORTES);
                persona.setRazonSocial(registroA.getNombreAportante());
                persona.setTipoIdentificacion(TipoIdentificacionEnum.obtenerTiposIdentificacionPILAEnum(registroA.getTipoIdAportante()));
                persona.setNumeroIdentificacion(registroA.getIdAportante());
                persona.setDigitoVerificacion(registroA.getDigVerAportante());
                ubicacion.setDireccionFisica(registroA.getDireccion());
                municipio.setIdMunicipio(new Short(registroA.getCodCiudad()));
                municipio.setIdDepartamento(new Short(registroA.getCodDepartamento()));
                ubicacion.setMunicipio(municipio);
                empresa.setFechaConstitucion(registroA.getFechaMatricula());
                empresa.setNaturalezaJuridica(
                        NaturalezaJuridicaEnum.obtenerNaturalezaJuridica(new Integer(registroA.getNaturalezaJuridica().shortValue())));
                persona.setUbicacionPrincipal(ubicacion);
                empresa.setPersona(persona);
                empleador.setEmpresa(empresa);
                entityManager.persist(empleador);
            }

            if (registroAP != null) {
                // se crea el aportante
                empleador.setEstadoEmpleador(EstadoEmpleadorEnum.NO_FORMALIZADO_SIN_AFILIACION_CON_APORTES);
                persona.setRazonSocial(registroAP.getNombrePagador());
                persona.setTipoIdentificacion(TipoIdentificacionEnum.obtenerTiposIdentificacionPILAEnum(registroAP.getTipoIdPagador()));
                persona.setNumeroIdentificacion(registroAP.getIdPagador());
                persona.setDigitoVerificacion(registroAP.getDigVerPagador());
                ubicacion.setDireccionFisica(registroAP.getDireccion());
                municipio.setIdMunicipio(new Short(registroAP.getCodCiudad()));
                municipio.setIdDepartamento(new Short(registroAP.getCodDepartamento()));
                ubicacion.setMunicipio(municipio);
                empresa.setNaturalezaJuridica(
                        NaturalezaJuridicaEnum.obtenerNaturalezaJuridica(new Integer(registroAP.getNaturalezaJuridica().shortValue())));
                persona.setUbicacionPrincipal(ubicacion);
                empresa.setPersona(persona);
                empleador.setEmpresa(empresa);
                entityManager.persist(empleador);
            }

        } catch (Exception e) {
            logger.error("Error al crear el empleador por favor revise los datos", e);
            logger.debug("Finaliza crearAportante(Long idPlanilla)");
            throw new TechnicalException(MensajesGeneralConstants.ERROR_TECNICO_INESPERADO);
        }

    }

    // inicio metodo de ayuda

    /**
     * Metodo que establece el estado de una inconsistencia como gestionada
     * 
     * @param IdErrorValidacion
     * @return Boolean
     */
    public Boolean establecerGestionInconsistencias(Long IdErrorValidacion) {
        try {
            // se realiza la busqueda de el error y se establece como gestionado
            ErrorValidacionLog result = entityManager
                    .createNamedQuery(NamedQueriesConstants.BUSQUEDA_ERROR_VALIDACION, ErrorValidacionLog.class)
                    .setParameter("idErrorValidacion", IdErrorValidacion).getSingleResult();
            result.setEstadoInconsistencia(EstadoGestionInconsistenciaEnum.INCONSISTENCIA_GESTIONADA);
            entityManager.merge(result);
            return true;
        } catch (Exception e) {
            throw new TechnicalException(MensajesGeneralConstants.ERROR_TECNICO_INESPERADO);
        }
    }

    /**
     * Metodo que establece que tipo de respuesta HTTP se le retorna a pantallas
     * 
     * @param result
     *        Lista que contiene el resultado de algunos de los servicios
     * 
     * @return List<code>InconsistenciaDTO</code> para tener el control sobre
     *         elementos vacios
     */
    private List<InconsistenciaDTO> evaluarResultado(List<InconsistenciaDTO> inconsistencias) {

        if (inconsistencias.isEmpty()) {
            return null;
        }

        return inconsistencias;
    }

    /**
     * Metodo que establece que tipo de operador es la inconsistencia
     * 
     * @param errores
     *        las inconsistencias existentes
     * @param tipo
     *        tipo de operador que se establece
     * @return List<TipoInconsistenciasEnum> contiene la lista con el tipo ya
     *         establecido
     */
    private List<InconsistenciaDTO> establecerOperador(List<InconsistenciaDTO> errores, String tipo) {
        if (tipo.equals("I")) {
            for (InconsistenciaDTO inconsistenciaDTO : errores) {
                inconsistenciaDTO.setTipoOperador(TipoOperadorEnum.OPERADOR_INFORMACION);
            }
            return errores;
        }
        for (InconsistenciaDTO inconsistenciaDTO : errores) {
            inconsistenciaDTO.setTipoOperador(TipoOperadorEnum.OPERADOR_FINANCIERO);
        }
        return errores;
    }

    /**
     * Metodo que establece la identificacion del aportante
     * 
     * @param errores
     *        las inconsistencias existentes
     * @param tipo
     *        tipo de operador que se establece
     * @return List<TipoInconsistenciasEnum> contiene la lista con el tipo ya
     *         establecido
     */
    private List<InconsistenciaDTO> establecerIdentificacionAportante(List<InconsistenciaDTO> errores) {
        // se recorre la lista para establecer el aportante tomando el dato del
        // nombre de archivo
        for (InconsistenciaDTO inconsistencia : errores) {

            inconsistencia.setNumeroIdAportante(inconsistencia.getNombreArchivo().split("_")[4]);
        }
        return errores;

    }

    /**
     * Metodo que establece que tipo de respuesta HTTP se le retorna a pantallas
     * 
     * @param result
     *        Lista que contiene el resultado de algunos de los servicios
     * 
     * @return List <code>IndiceCorreccionPlanilla</code>
     */
    private List<IndiceCorreccionPlanilla> evaluarResultadoSolicitudes(List<IndiceCorreccionPlanilla> solicitudes) {

        if (solicitudes.isEmpty()) {
            return null;
        }

        return solicitudes;
    }

    /**
     * Metodo que establece que pestañas va a poseer el detalle de las
     * inconsistencias
     * 
     * @param bloques
     * @return List<TipoInconsistenciasEnum> con las pestañas activas
     */
    private List<TipoInconsistenciasEnum> generarPestañas(List<BloqueValidacionEnum> bloques) {
        List<TipoInconsistenciasEnum> result = new ArrayList<TipoInconsistenciasEnum>();

        for (BloqueValidacionEnum bloqueValidacionEnum : bloques) {
            if (bloqueValidacionEnum.getGrupoInconsistencia() != null) {
                result.add(bloqueValidacionEnum.getGrupoInconsistencia());
            }
        }

        return result;
    }

    /**
     * Metodo que establece las diferencias entre archivo I y F
     * 
     * @param result
     *        List <code>InconsistenciaDTO</code> que contiene los datos
     *        para realizar la diferencia entre las dos planillas
     * @return List <code>InconsistenciaDTO</code> con los datos de las
     *         diferencias ya establecidos
     */
    private List<InconsistenciaDTO> establecerCamposConciliacion(List<InconsistenciaDTO> result) {
        for (InconsistenciaDTO inconsistencia : result) {
            String[] campos = inconsistencia.getValorCampo().split(":-:");
            if (campos.length == 2) {
                inconsistencia.setValorCampo(campos[0]);
                inconsistencia.setValorCampoFinanciero(campos[1]);
            }
        }
        return result;
    }

    /**
     * Metodo que establece la descripcion completa de la inconsistencia
     * 
     * @param result
     *        lista con las inconsistencias
     * @return List<TipoInconsistenciasEnum> Lista con los campos ya
     *         establecidos de las diferencias
     */
    private List<InconsistenciaDTO> establecerDetalleInconsistencia(List<InconsistenciaDTO> result) {
        for (InconsistenciaDTO inconsistencia : result) {
            inconsistencia.setNumeroReprocesos("" + entityManager.createNamedQuery(NamedQueriesConstants.CONTEO_REPROCESOS_PLANILLA)
                    .setParameter("idPlanilla", inconsistencia.getIndicePlanilla()).getSingleResult());
        }
        return result;
    }

    /**
     * Metodo que establece como anulado el {@link EstadoArchivoPorBloque}
     * asociado un indicePlanilla
     * 
     * @param bloque
     * @param estado
     * @return el mismo {@link EstadoArchivoPorBloque}
     */
    private EstadoArchivoPorBloque establecerUbicacionBloqueAnular(BloqueValidacionEnum bloque, EstadoArchivoPorBloque estado) {

        if (bloque == BloqueValidacionEnum.BLOQUE_0_OI) {
            estado.setAccionBloque0(AccionProcesoArchivoEnum.ARCHIVO_ANULADO);
            estado.setEstadoBloque0(EstadoProcesoArchivoEnum.ANULADO);
            return estado;
        }

        if (bloque == BloqueValidacionEnum.BLOQUE_1_OI) {
            estado.setAccionBloque1(AccionProcesoArchivoEnum.ARCHIVO_ANULADO);
            estado.setEstadoBloque1(EstadoProcesoArchivoEnum.ANULADO);
            return estado;
        }
        if (bloque == BloqueValidacionEnum.BLOQUE_2_OI) {
            estado.setAccionBloque2(AccionProcesoArchivoEnum.ARCHIVO_ANULADO);
            estado.setEstadoBloque2(EstadoProcesoArchivoEnum.ANULADO);
            return estado;
        }
        if (bloque == BloqueValidacionEnum.BLOQUE_3_OI) {
            estado.setAccionBloque3(AccionProcesoArchivoEnum.ARCHIVO_ANULADO);
            estado.setEstadoBloque3(EstadoProcesoArchivoEnum.ANULADO);
            return estado;
        }
        if (bloque == BloqueValidacionEnum.BLOQUE_4_OI) {
            estado.setAccionBloque4(AccionProcesoArchivoEnum.ARCHIVO_ANULADO);
            estado.setEstadoBloque4(EstadoProcesoArchivoEnum.ANULADO);
            return estado;
        }
        if (bloque == BloqueValidacionEnum.BLOQUE_5_OI) {
            estado.setAccionBloque5(AccionProcesoArchivoEnum.ARCHIVO_ANULADO);
            estado.setEstadoBloque5(EstadoProcesoArchivoEnum.ANULADO);
            return estado;
        }
        if (bloque == BloqueValidacionEnum.BLOQUE_6_OI) {
            estado.setAccionBloque6(AccionProcesoArchivoEnum.ARCHIVO_ANULADO);
            estado.setEstadoBloque6(EstadoProcesoArchivoEnum.ANULADO);
            return estado;
        }
        if (bloque == BloqueValidacionEnum.BLOQUE_7_OI) {
            estado.setAccionBloque7(AccionProcesoArchivoEnum.ARCHIVO_ANULADO);
            estado.setEstadoBloque7(EstadoProcesoArchivoEnum.ANULADO);
            return estado;
        }
        return null;
    }

    /**
     * Metodo que establece la ubicacion de un bloque de validacion OF luego de
     * haber sido ejecutado la accion de anular
     * 
     * @param bloque
     *        <code>BloqueValidacionEnum</code> Bloque actual en el que se
     *        encuentra el archivo OF
     * @param estado
     *        <code>EstadoArchivoPorBloqueOF</code> Se establece los datos
     *        necesarios dependiendo del bloque
     * @return estado <code>EstadoArchivoPorBloqueOF</code>
     */
    private EstadoArchivoPorBloqueOF establecerUbicacionBloqueAnularOF(BloqueValidacionEnum bloque, EstadoArchivoPorBloqueOF estado) {
        if (bloque == BloqueValidacionEnum.BLOQUE_0_OF) {
            estado.setAccionBloque0(AccionProcesoArchivoEnum.ARCHIVO_ANULADO);
            estado.setEstadoBloque0(EstadoProcesoArchivoEnum.ANULADO);
            return estado;
        }

        if (bloque == BloqueValidacionEnum.BLOQUE_1_OF) {
            estado.setAccionBloque1(AccionProcesoArchivoEnum.ARCHIVO_ANULADO);
            estado.setEstadoBloque1(EstadoProcesoArchivoEnum.ANULADO);
            return estado;
        }

        return null;
    }

    /**
     * Metodo que establece la accion y el estado de un
     * {@link EstadoArchivoPorBloque}
     * 
     * @param bloque
     * @param estado
     * @return {@link EstadoArchivoPorBloque} con los campos establecidos
     */
    private EstadoArchivoPorBloque establecerUbicacionBloqueSiguienteBloque(BloqueValidacionEnum bloque, EstadoArchivoPorBloque estado) {
        if (bloque == BloqueValidacionEnum.BLOQUE_0_OF) {
            estado.setAccionBloque0(AccionProcesoArchivoEnum.EJECUTAR_BLOQUE_1);
            estado.setEstadoBloque0(EstadoProcesoArchivoEnum.ESTRUCTURA_VALIDADA);
            return estado;
        }

        if (bloque == BloqueValidacionEnum.BLOQUE_1_OF) {
            estado.setAccionBloque1(AccionProcesoArchivoEnum.ARCHIVO_ANULADO);
            estado.setEstadoBloque1(EstadoProcesoArchivoEnum.ESTRUCTURA_VALIDADA);
            return estado;
        }
        if (bloque == BloqueValidacionEnum.BLOQUE_1_OI) {
            estado.setAccionBloque1(AccionProcesoArchivoEnum.EJECUTAR_BLOQUE_2);
            estado.setEstadoBloque1(EstadoProcesoArchivoEnum.ESTRUCTURA_VALIDADA);
            return estado;
        }
        if (bloque == BloqueValidacionEnum.BLOQUE_2_OI) {
            estado.setAccionBloque2(AccionProcesoArchivoEnum.EJECUTAR_BLOQUE_3);
            estado.setEstadoBloque2(EstadoProcesoArchivoEnum.ESTRUCTURA_VALIDADA);
            return estado;
        }
        if (bloque == BloqueValidacionEnum.BLOQUE_3_OI) {
            estado.setAccionBloque3(AccionProcesoArchivoEnum.EJECUTAR_BLOQUE_4);
            estado.setEstadoBloque3(EstadoProcesoArchivoEnum.PAREJA_DE_ARCHIVOS_CONSISTENTES);
            return estado;
        }

        if (bloque == BloqueValidacionEnum.BLOQUE_5_OI) {
            estado.setAccionBloque5(AccionProcesoArchivoEnum.EJECUTAR_BLOQUE_6);
            estado.setEstadoBloque5(EstadoProcesoArchivoEnum.ARCHIVO_CONSISTENTE);
            return estado;
        }
        if (bloque == BloqueValidacionEnum.BLOQUE_6_OI) {
            estado.setAccionBloque6(AccionProcesoArchivoEnum.PASAR_A_CRUCE_CON_BD);
            estado.setEstadoBloque6(EstadoProcesoArchivoEnum.RECAUDO_CONCILIADO);
            return estado;
        }

        return null;
    }

    /**
     * Metodo que establece un nombre con la concatenacion de los nombres de los
     * indices
     * 
     * @param indiceI
     * @param indiceA
     * @param indiceF
     * @return {@link String} con el nombre de los archivos concatenados
     */
    private String generarStringArchivosAsociados(IndicePlanilla indiceI, IndicePlanilla indiceA, IndicePlanillaOF indiceF) {
        String result = "" + indiceI.getNombreArchivo() + ",";
        result = result + " " + indiceA.getNombreArchivo() + ",";
        result = result + " " + indiceF.getNombreArchivo();

        return result;
    }

    /**
     * Metodo que modifica el nombre de un archivo con los datos nuevos
     * 
     * @param numeroIdentificacionNuevo
     * @param numeroIdentificacion
     * @param nombreArchivo
     * @return {@link String} Con el nuevo nombre del archivo
     */
    private String modificarNombreArchivo(Long numeroIdentificacionNuevo, String numeroIdentificacion, String nombreArchivo) {
        String[] partesNombre = nombreArchivo.split("_");

        for (int i = 0; i < partesNombre.length; i++) {
            if (partesNombre[i].equals(numeroIdentificacion)) {
                partesNombre[i] = ("" + numeroIdentificacionNuevo);
            }
        }

        StringJoiner unirPartes = new StringJoiner("_");

        for (String parte : partesNombre) {
            unirPartes.add(parte);
        }

        return "" + unirPartes;
    }

    /**
     * Metodo que filtra los datos a mostrar a un usuario en la lista de
     * consulta de solicitudes de cambio de identficacion
     * 
     * @param listaSolicitudes
     * @return una lista de {@link IndiceCorreccionPlanilla} con los que no han
     *         sido aprobados o rechazados
     */
    private List<IndiceCorreccionPlanilla> filtrarResultadoConsultaSolicitudes(List<IndiceCorreccionPlanilla> listaSolicitudes) {
        if (listaSolicitudes != null) {
            List<IndiceCorreccionPlanilla> listaResult = new ArrayList<IndiceCorreccionPlanilla>();
            for (IndiceCorreccionPlanilla solicitud : listaSolicitudes) {
                if (solicitud.getUsuarioAprobador() == null) {
                    listaResult.add(solicitud);
                }
            }

            return listaResult;
        }
        return null;

    }

    /**
     * (non-Javadoc)
     * 
     * @see com.asopagos.bandejainconsistencias.service.PilaBandejaService#veArchivo(java.lang.Long)
     */
    @Override
    public IdentificadorDocumentoDTO veArchivo(Long idPlanilla) {
        IndicePlanilla indiceOI = new IndicePlanilla();
        try {
            indiceOI = entityManager.createNamedQuery(NamedQueriesConstants.OBTENER_INDICE_PLANILLAS, IndicePlanilla.class)
                    .setParameter("idIndicePlanilla", idPlanilla).getSingleResult();
            if (indiceOI != null) {
                return new IdentificadorDocumentoDTO(indiceOI.getIdDocumento(), indiceOI.getVersionDocumento());
            }
        } catch (NoResultException e) {
            indiceOI = null;
        }

        try {
            IndicePlanillaOF indiceOF = entityManager
                    .createNamedQuery(NamedQueriesConstants.OBTENER_INDICE_PLANILLAS_OF, IndicePlanillaOF.class)
                    .setParameter("idIndicePlanilla", idPlanilla).getSingleResult();
            if (indiceOF != null) {
                return new IdentificadorDocumentoDTO(indiceOF.getIdDocumento(), indiceOF.getVersionDocumento());
            }
        } catch (NoResultException e) {
            logger.error("No se encuentra el archivo solicitado", e);
            logger.debug("Finaliza veArchivo(Long idPlanilla)");
            throw new TechnicalException(MensajesGeneralConstants.ERROR_RECURSO_NO_ENCONTRADO);
        }
        return null;
    }

    /**
     * Metodo que mapea el resultado de una consulta nativa para la HU 392
     * 
     * @param consulta
     *        List<code>Object[]</code> Contiene el resultado de una
     *        consulta nativa
     * @return resultado List<code>InconsistenciaDTO</code> contiene una lista
     *         con los datos ya mapeados en el DTO
     */
    public List<InconsistenciaDTO> mapeoInconsistenciaDTO(List<Object[]> consulta) {
        // TODO Auto-generated method stub
        List<InconsistenciaDTO> resultado = new ArrayList<InconsistenciaDTO>();

        for (Object[] objeto : consulta) {
            InconsistenciaDTO dato = new InconsistenciaDTO();
            dato.setFechaProcesamiento((Date) objeto[0]);
            dato.setNumeroPlanilla(((BigInteger) objeto[1]).longValueExact());
            dato.setNombreArchivo((String) objeto[2]);
            dato.setTipoArchivo(TipoArchivoPilaEnum.valueOf((String) objeto[3]));
            dato.setCantidadErrores(new Long((Integer) objeto[4]));
            dato.setEstadoArchivo(EstadoProcesoArchivoEnum.valueOf((String) objeto[5]));

            resultado.add(dato);
        }

        return resultado;

    }

    /**
     * Metodo que mapea los datos de una consulta nativa en un DTO
     * 
     * @param consulta
     *        List<code>Object[]</code> Contiene el resultado de una
     *        consulta nativa
     * @return resultado List<code>RespuestaConsultaEmpleadorDTO</code> Lista
     *         que contiene los datos ya mapeados en un DTO
     */
    public List<RespuestaConsultaEmpleadorDTO> mapeoAportesEmpresa(List<Object[]> consulta) {
        // TODO Auto-generated method stub
        List<RespuestaConsultaEmpleadorDTO> resultado = new ArrayList<RespuestaConsultaEmpleadorDTO>();
        List<DetalleTablaAportanteDTO> detalles = new ArrayList<DetalleTablaAportanteDTO>();
        // List<Object[]> consulta =
        // entityManager.createNamedQuery(NamedQueriesConstants.PRUEBA).getResultList();

        if (consulta.isEmpty()) {
            return resultado;
        }
        else {
            // se establecen los datos principales del DTO que serian los datos
            // de cabecera
            String nombreAportante = (String) consulta.get(0)[0];
            TipoIdentificacionEnum tipoIdentificacion = null;
            String numeroIdentificacion = null;
            String periodoAporte = (String) consulta.get(0)[14];

            for (Object[] objeto : consulta) {
                if (((String) objeto[14]).equalsIgnoreCase(periodoAporte) == false) {
                    // se establecen los datos finales de la cabecera
                    RespuestaConsultaEmpleadorDTO result = new RespuestaConsultaEmpleadorDTO();
                    result.setNombreEmpleador(nombreAportante);
                    result.setTipoIdentificacion(tipoIdentificacion);
                    result.setNumeroIdentificacion(numeroIdentificacion);
                    String[] castPeriodo = periodoAporte.split("-");
                    Calendar calendar = new GregorianCalendar();
                    calendar.set(Integer.parseInt(castPeriodo[0]), Integer.parseInt(castPeriodo[1]), 0, 0, 0, 0);
                    result.setPeriodoAporte(calendar.getTime());
                    result.setRegistros(detalles);
                    resultado.add(result);
                    detalles = new ArrayList<DetalleTablaAportanteDTO>();

                }
                // se establecen los datos de los cotizantes
                DetalleTablaAportanteDTO detalle = new DetalleTablaAportanteDTO();
                nombreAportante = (String) objeto[0];
                tipoIdentificacion = objeto[12] != null ? (TipoIdentificacionEnum.obtenerTiposIdentificacionPILAEnum((String) objeto[12]))
                        : null;
                numeroIdentificacion = (String) objeto[13];
                periodoAporte = (String) objeto[14];
                detalle.setIdCotizante((String) objeto[1]);
                detalle.setSecuencia((Integer) objeto[2]);
                detalle.setIdPlanilla(((BigInteger) objeto[3]).longValueExact());
                detalle.setTipoArchivo(TipoArchivoPilaEnum.valueOf((String) objeto[4]));
                detalle.setFechaProcesamiento((Date) objeto[5]);
                detalle.setAporteObligatorio((Integer) objeto[6]);
                detalle.setTipoCotizante((String) objeto[7]);
                detalle.setV0(objeto[8] != null ? (EstadoValidacionRegistroAporteEnum.valueOf((String) objeto[8])) : null);
                detalle.setV1(objeto[9] != null ? (EstadoValidacionRegistroAporteEnum.valueOf((String) objeto[9])) : null);
                detalle.setV2(objeto[10] != null ? (EstadoValidacionRegistroAporteEnum.valueOf((String) objeto[10])) : null);
                detalle.setV3(objeto[11] != null ? (EstadoValidacionRegistroAporteEnum.valueOf((String) objeto[11])) : null);
                detalle.setTipoIdCotizante(TipoIdentificacionEnum.obtenerTiposIdentificacionPILAEnum((String) objeto[15]));
                detalle.setIndicePlanilla(((BigInteger) objeto[16]).longValueExact());
                detalle.setEstadoRegistro((String) objeto[17]);
                detalles.add(detalle);

            }

            RespuestaConsultaEmpleadorDTO result = new RespuestaConsultaEmpleadorDTO();
            result.setNombreEmpleador(nombreAportante);
            result.setTipoIdentificacion(tipoIdentificacion);
            result.setNumeroIdentificacion(numeroIdentificacion);
            String[] castPeriodo = periodoAporte.split("-");
            Calendar calendar = new GregorianCalendar();
            calendar.set(Integer.parseInt(castPeriodo[0]), Integer.parseInt(castPeriodo[1]), 0, 0, 0, 0);
            result.setPeriodoAporte(calendar.getTime());
            result.setRegistros(detalles);
            resultado.add(result);
        }

        return resultado;

    }

    /**
     * Metodo que mapea los datos de una consulta nativa en un DTO
     * 
     * @param consulta
     *        List<code>Object[]</code> Contiene el resultado de una
     *        consulta nativa
     * @return resultado List<code>RespuestaConsultaEmpleadorDTO</code> Lista
     *         que contiene los datos ya mapeados en un DTO
     */
    public List<RespuestaConsultaEmpleadorDTO> mapeoAportesEmpresaPensionados(List<Object[]> consulta) {
        // TODO Auto-generated method stub
        List<RespuestaConsultaEmpleadorDTO> resultado = new ArrayList<RespuestaConsultaEmpleadorDTO>();
        List<DetalleTablaAportanteDTO> detalles = new ArrayList<DetalleTablaAportanteDTO>();
        // List<Object[]> consulta =
        // entityManager.createNamedQuery(NamedQueriesConstants.PRUEBA).getResultList();

        if (consulta.isEmpty()) {
            return resultado;
        }
        else {

            String nombreAportante = (String) consulta.get(0)[0];
            TipoIdentificacionEnum tipoIdentificacion = null;
            String numeroIdentificacion = null;
            String periodoAporte = (String) consulta.get(0)[11];
            for (Object[] objeto : consulta) {
                if (((String) objeto[11]).equalsIgnoreCase(periodoAporte) == false) {
                    RespuestaConsultaEmpleadorDTO result = new RespuestaConsultaEmpleadorDTO();
                    result.setNombreEmpleador(nombreAportante);
                    result.setTipoIdentificacion(tipoIdentificacion);
                    result.setNumeroIdentificacion(numeroIdentificacion);
                    String[] castPeriodo = periodoAporte.split("-");
                    Calendar calendar = new GregorianCalendar();
                    calendar.set(Integer.parseInt(castPeriodo[0]), Integer.parseInt(castPeriodo[1]), 0, 0, 0, 0);
                    result.setPeriodoAporte(calendar.getTime());
                    result.setRegistros(detalles);
                    resultado.add(result);
                    detalles = new ArrayList<DetalleTablaAportanteDTO>();

                }
                DetalleTablaAportanteDTO detalle = new DetalleTablaAportanteDTO();
                nombreAportante = (String) objeto[0];
                tipoIdentificacion = objeto[9] != null ? (TipoIdentificacionEnum.obtenerTiposIdentificacionPILAEnum((String) objeto[9]))
                        : null;
                numeroIdentificacion = (String) objeto[10];
                periodoAporte = (String) objeto[11];
                detalle.setIdCotizante((String) objeto[1]);
                detalle.setSecuencia((Integer) objeto[2]);
                detalle.setIdPlanilla(((BigInteger) objeto[3]).longValueExact());
                detalle.setTipoArchivo(TipoArchivoPilaEnum.valueOf((String) objeto[4]));
                detalle.setFechaProcesamiento((Date) objeto[5]);
                detalle.setAporteObligatorio((Integer) objeto[6]);
                detalle.setTipoCotizante((String) objeto[7]);
                detalle.setV1(objeto[8] != null ? (EstadoValidacionRegistroAporteEnum.valueOf((String) objeto[8])) : null);
                detalle.setTipoIdCotizante(TipoIdentificacionEnum.obtenerTiposIdentificacionPILAEnum((String) objeto[12]));
                detalle.setIndicePlanilla(((BigInteger) objeto[13]).longValueExact());
                detalle.setEstadoRegistro((String) objeto[14]);

            }
            RespuestaConsultaEmpleadorDTO result = new RespuestaConsultaEmpleadorDTO();
            result.setNombreEmpleador(nombreAportante);
            result.setTipoIdentificacion(tipoIdentificacion);
            result.setNumeroIdentificacion(numeroIdentificacion);
            String[] castPeriodo = periodoAporte.split("-");
            Calendar calendar = new GregorianCalendar();
            calendar.set(Integer.parseInt(castPeriodo[0]), Integer.parseInt(castPeriodo[1]), 0, 0, 0, 0);
            result.setPeriodoAporte(calendar.getTime());
            result.setRegistros(detalles);
            resultado.add(result);
        }

        return resultado;

    }

    /**
     * (non-Javadoc)
     * 
     * @see com.asopagos.bandejainconsistencias.service.PilaBandejaService#contarPlanillasConInconsistenciasPorGestionar()
     */
    @Override
    public Integer contarPlanillasConInconsistenciasPorGestionar() {
        logger.debug("Inicia contarPlanillasConInconsistenciasPorGestionar()");
        Integer numPlanillas = null;
        try {
            numPlanillas = (Integer) entityManager.createNamedQuery(NamedQueriesConstants.CONTAR_PLANILLAS_CON_INCONSISTENCIAS)
                    .getSingleResult();
        } catch (NoResultException e) {
            logger.debug("No se encuentra una planilla con inconsistencias");
        }
        logger.debug("Finaliza contarPlanillasConInconsistenciasPorGestionar()");
        return numPlanillas;
    }

    /**
     * (non-Javadoc)
     * 
     * @see com.asopagos.bandejainconsistencias.service.PilaBandejaService#consultarPlanillasPorGestionarConInconsistenciasValidacion(com.asopagos.enumeraciones.personas.TipoIdentificacionEnum,
     *      java.lang.String, java.lang.Short, java.lang.Long, java.lang.Long)
     */
    @Override
    public List<InconsistenciaRegistroAporteDTO> consultarPlanillasPorGestionarConInconsistenciasValidacion(
            TipoIdentificacionEnum tipoIdentificacionAportante, String numeroIdentificacionAportante, Short digitoVerificacionAportante,
            Long fechaInicio, Long fechaFin) {

        List<InconsistenciaRegistroAporteDTO> lstInconsistenciaRegistroAporteDTO = null;
        List<InconsistenciaRegistroAporteDTO> lstResultIndDep = null;
        List<InconsistenciaRegistroAporteDTO> lstResultPen = null;
        Date fechaInicioAporte = null;
        Date fechaFinalAporte = null;

        if (TipoIdentificacionEnum.NIT.equals(tipoIdentificacionAportante) && digitoVerificacionAportante == null) {
            // TODO ERROR 1-Validar que si viene nit el campo DV debe contener
            // valor
        }
        else if (TipoIdentificacionEnum.NIT.equals(tipoIdentificacionAportante) && digitoVerificacionAportante == 0) {
            // TODO ERROR 1-el digito de verificacion no puede ser cero.
        }

        if (fechaInicio == null && fechaFin == null) {
            // TODO ERROR DEBE LLEGAR ALGUNA O LAS DOS FECHAS
        }
        else if (fechaInicio != null && fechaFin == null) {
            fechaInicioAporte = new Date(fechaInicio);
            // Si no viene fecha fin del procesamiento, esta se asume como la
            // actual
            fechaFinalAporte = Calendar.getInstance().getTime();
        }
        else if (fechaInicio == null && fechaFin != null) {
            // Se hace fecha inicio la fecha limite incial definida en la HU;
            // para realizar una sola consulta.
            SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");
            try {
                fechaInicioAporte = format.parse("1900/01/01");
            } catch (ParseException e) {
            }
            // Se realiza la consulta solo con fecha fin del procesamiento
            fechaFinalAporte = new Date(fechaFin);
        }
        else {
            // Se establecen las fechas en formato Date para las consultas
            fechaInicioAporte = new Date(fechaInicio);
            fechaFinalAporte = new Date(fechaFin);
        }
        // se realiza la consulta de los registrosde aporte de planillas que
        // tienen inconsistencias por gestionar
        if (tipoIdentificacionAportante == null && numeroIdentificacionAportante == null && fechaInicio == null && fechaFin == null) {
            lstInconsistenciaRegistroAporteDTO = new ArrayList<InconsistenciaRegistroAporteDTO>();
            lstResultIndDep = entityManager
                    .createNamedQuery(NamedQueriesConstants.CONSULTAR_REGISTROS_APORTE_PLANILLAS_CON_INCONSISTENCIAS_IND_DEP_SIN_FILTRO,
                            InconsistenciaRegistroAporteDTO.class)
                    .getResultList();
            lstResultPen = entityManager
                    .createNamedQuery(NamedQueriesConstants.CONSULTAR_REGISTROS_APORTE_PLANILLAS_CON_INCONSISTENCIAS_PEN_SIN_FILTRO,
                            InconsistenciaRegistroAporteDTO.class)
                    .getResultList();
            lstInconsistenciaRegistroAporteDTO.addAll(lstResultIndDep);
            lstInconsistenciaRegistroAporteDTO.addAll(lstResultPen);
        }
        else if (TipoIdentificacionEnum.NIT.equals(tipoIdentificacionAportante)) {
            // busqueda con todo el filtro
            lstInconsistenciaRegistroAporteDTO = new ArrayList<InconsistenciaRegistroAporteDTO>();
            lstResultIndDep = entityManager
                    .createNamedQuery(NamedQueriesConstants.CONSULTAR_REGISTROS_APORTE_PLANILLAS_CON_INCONSISTENCIAS_IND_DEP,
                            InconsistenciaRegistroAporteDTO.class)
                    .setParameter("tipoIdentificacionAportante", tipoIdentificacionAportante.getValorEnPILA())
                    .setParameter("numeroIdentificacionAportante", numeroIdentificacionAportante)
                    .setParameter("digitoVerificacionAportante", digitoVerificacionAportante)
                    .setParameter("fechaInicioAporte", CalendarUtils.truncarHora(fechaInicioAporte))
                    .setParameter("fechaFinalAporte", CalendarUtils.truncarHoraMaxima(fechaFinalAporte)).getResultList();

            lstResultPen = entityManager
                    .createNamedQuery(NamedQueriesConstants.CONSULTAR_REGISTROS_APORTE_PLANILLAS_CON_INCONSISTENCIAS_PEN,
                            InconsistenciaRegistroAporteDTO.class)
                    .setParameter("tipoIdentificacionAportante", tipoIdentificacionAportante.getValorEnPILA())
                    .setParameter("numeroIdentificacionAportante", numeroIdentificacionAportante)
                    .setParameter("digitoVerificacionAportante", digitoVerificacionAportante)
                    .setParameter("fechaInicioAporte", CalendarUtils.truncarHora(fechaInicioAporte))
                    .setParameter("fechaFinalAporte", CalendarUtils.truncarHoraMaxima(fechaFinalAporte)).getResultList();
            lstInconsistenciaRegistroAporteDTO.addAll(lstResultIndDep);
            lstInconsistenciaRegistroAporteDTO.addAll(lstResultPen);
        }
        else if (digitoVerificacionAportante == null) {
            lstInconsistenciaRegistroAporteDTO = new ArrayList<InconsistenciaRegistroAporteDTO>();
            // TODO busqueda sin digito de verificacion en el filtro
            lstInconsistenciaRegistroAporteDTO = new ArrayList<InconsistenciaRegistroAporteDTO>();
            lstResultIndDep = entityManager
                    .createNamedQuery(NamedQueriesConstants.CONSULTAR_REGISTROS_APORTE_PLANILLAS_CON_INCONSISTENCIAS_IND_DEP_SIN_DV,
                            InconsistenciaRegistroAporteDTO.class)
                    .setParameter("tipoIdentificacionAportante", tipoIdentificacionAportante.getValorEnPILA())
                    .setParameter("numeroIdentificacionAportante", numeroIdentificacionAportante)
                    .setParameter("fechaInicioAporte", CalendarUtils.truncarHora(fechaInicioAporte))
                    .setParameter("fechaFinalAporte", CalendarUtils.truncarHoraMaxima(fechaFinalAporte)).getResultList();

            lstResultPen = entityManager
                    .createNamedQuery(NamedQueriesConstants.CONSULTAR_REGISTROS_APORTE_PLANILLAS_CON_INCONSISTENCIAS_PEN_SIN_DV,
                            InconsistenciaRegistroAporteDTO.class)
                    .setParameter("tipoIdentificacionAportante", tipoIdentificacionAportante.getValorEnPILA())
                    .setParameter("numeroIdentificacionAportante", numeroIdentificacionAportante)
                    .setParameter("fechaInicioAporte", CalendarUtils.truncarHora(fechaInicioAporte))
                    .setParameter("fechaFinalAporte", CalendarUtils.truncarHoraMaxima(fechaFinalAporte)).getResultList();
            lstInconsistenciaRegistroAporteDTO.addAll(lstResultIndDep);
            lstInconsistenciaRegistroAporteDTO.addAll(lstResultPen);
        }
        return lstInconsistenciaRegistroAporteDTO;
    }

    /**
     * (non-Javadoc)
     * 
     * @see com.asopagos.bandejainconsistencias.service.PilaBandejaService#consultarPlanillasPorAprobarGestionarConInconsistenciasValidacion(com.asopagos.enumeraciones.personas.TipoIdentificacionEnum,
     *      java.lang.String, java.lang.Short, java.lang.Long, java.lang.Long)
     */
    @Override
    public List<InconsistenciaRegistroAporteDTO> consultarPlanillasPorAprobarGestionarConInconsistenciasValidacion(
            TipoIdentificacionEnum tipoIdentificacionAportante, String numeroIdentificacionAportante, Short digitoVerificacionAportante,
            Long fechaInicio, Long fechaFin) {

        List<InconsistenciaRegistroAporteDTO> lstInconsistenciaRegistroAporteDTO = null;
        List<InconsistenciaRegistroAporteDTO> lstResultIndDep = null;
        List<InconsistenciaRegistroAporteDTO> lstResultPen = null;
        Date fechaInicioAporte = null;
        Date fechaFinalAporte = null;

        if (TipoIdentificacionEnum.NIT.equals(tipoIdentificacionAportante) && digitoVerificacionAportante == null) {
            // TODO ERROR 1-Validar que si viene nit el campo DV debe contener
            // valor
        }
        else if (TipoIdentificacionEnum.NIT.equals(tipoIdentificacionAportante) && digitoVerificacionAportante == 0) {
            // TODO ERROR 1-el digito de verificacion no puede ser cero.
        }

        if (fechaInicio == null && fechaFin == null) {
            // TODO ERROR DEBE LLEGAR ALGUNA O LAS DOS FECHAS
        }
        else if (fechaInicio != null && fechaFin == null) {
            fechaInicioAporte = new Date(fechaInicio);
            // Si no viene fecha fin del procesamiento, esta se asume como la
            // actual
            fechaFinalAporte = Calendar.getInstance().getTime();
        }
        else if (fechaInicio == null && fechaFin != null) {
            // Se hace fecha inicio la fecha limite incial definida en la HU;
            // para realizar una sola consulta.
            SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");
            try {
                fechaInicioAporte = format.parse("1900/01/01");
            } catch (ParseException e) {
            }
            // Se realiza la consulta solo con fecha fin del procesamiento
            fechaFinalAporte = new Date(fechaFin);
        }
        else {
            // Se establecen las fechas en formato Date para las consultas
            fechaInicioAporte = new Date(fechaInicio);
            fechaFinalAporte = new Date(fechaFin);
        }
        // se realiza la consulta de los registrosde aporte de planillas que
        // tienen inconsistencias por gestionar
        if (tipoIdentificacionAportante == null && numeroIdentificacionAportante == null && fechaInicio == null && fechaFin == null) {
            lstInconsistenciaRegistroAporteDTO = new ArrayList<InconsistenciaRegistroAporteDTO>();
            lstResultIndDep = entityManager.createNamedQuery(
                    NamedQueriesConstants.CONSULTAR_REGISTROS_APORTE_PLANILLAS_CON_INCONSISTENCIAS_POR_GESTIONAR_IND_DEP_SIN_FILTRO,
                    InconsistenciaRegistroAporteDTO.class).getResultList();
            lstResultPen = entityManager.createNamedQuery(
                    NamedQueriesConstants.CONSULTAR_REGISTROS_APORTE_PLANILLAS_CON_INCONSISTENCIAS_POR_GESTIONAR_PEN_SIN_FILTRO,
                    InconsistenciaRegistroAporteDTO.class).getResultList();
            lstInconsistenciaRegistroAporteDTO.addAll(lstResultIndDep);
            lstInconsistenciaRegistroAporteDTO.addAll(lstResultPen);
        }
        else if (TipoIdentificacionEnum.NIT.equals(tipoIdentificacionAportante)) {
            // busqueda con todo el filtro
            lstInconsistenciaRegistroAporteDTO = new ArrayList<InconsistenciaRegistroAporteDTO>();
            lstResultIndDep = entityManager
                    .createNamedQuery(NamedQueriesConstants.CONSULTAR_REGISTROS_APORTE_PLANILLAS_CON_INCONSISTENCIAS_POR_GESTIONAR_IND_DEP,
                            InconsistenciaRegistroAporteDTO.class)
                    .setParameter("tipoIdentificacionAportante", tipoIdentificacionAportante.getValorEnPILA())
                    .setParameter("numeroIdentificacionAportante", numeroIdentificacionAportante)
                    .setParameter("digitoVerificacionAportante", digitoVerificacionAportante)
                    .setParameter("fechaInicioAporte", CalendarUtils.truncarHora(fechaInicioAporte))
                    .setParameter("fechaFinalAporte", CalendarUtils.truncarHoraMaxima(fechaFinalAporte)).getResultList();

            lstResultPen = entityManager
                    .createNamedQuery(NamedQueriesConstants.CONSULTAR_REGISTROS_APORTE_PLANILLAS_CON_INCONSISTENCIAS_POR_GESTIONAR_PEN,
                            InconsistenciaRegistroAporteDTO.class)
                    .setParameter("tipoIdentificacionAportante", tipoIdentificacionAportante.getValorEnPILA())
                    .setParameter("numeroIdentificacionAportante", numeroIdentificacionAportante)
                    .setParameter("digitoVerificacionAportante", digitoVerificacionAportante)
                    .setParameter("fechaInicioAporte", CalendarUtils.truncarHora(fechaInicioAporte))
                    .setParameter("fechaFinalAporte", CalendarUtils.truncarHoraMaxima(fechaFinalAporte)).getResultList();
            lstInconsistenciaRegistroAporteDTO.addAll(lstResultIndDep);
            lstInconsistenciaRegistroAporteDTO.addAll(lstResultPen);
        }
        else if (digitoVerificacionAportante == null) {
            lstInconsistenciaRegistroAporteDTO = new ArrayList<InconsistenciaRegistroAporteDTO>();
            // TODO busqueda sin digito de verificacion en el filtro
            lstInconsistenciaRegistroAporteDTO = new ArrayList<InconsistenciaRegistroAporteDTO>();
            lstResultIndDep = entityManager
                    .createNamedQuery(
                            NamedQueriesConstants.CONSULTAR_REGISTROS_APORTE_PLANILLAS_CON_INCONSISTENCIAS_POR_GESTIONAR_IND_DEP_SIN_DV,
                            InconsistenciaRegistroAporteDTO.class)
                    .setParameter("tipoIdentificacionAportante", tipoIdentificacionAportante.getValorEnPILA())
                    .setParameter("numeroIdentificacionAportante", numeroIdentificacionAportante)
                    .setParameter("fechaInicioAporte", CalendarUtils.truncarHora(fechaInicioAporte))
                    .setParameter("fechaFinalAporte", CalendarUtils.truncarHoraMaxima(fechaFinalAporte)).getResultList();

            lstResultPen = entityManager
                    .createNamedQuery(
                            NamedQueriesConstants.CONSULTAR_REGISTROS_APORTE_PLANILLAS_CON_INCONSISTENCIAS_POR_GESTIONAR_PEN_SIN_DV,
                            InconsistenciaRegistroAporteDTO.class)
                    .setParameter("tipoIdentificacionAportante", tipoIdentificacionAportante.getValorEnPILA())
                    .setParameter("numeroIdentificacionAportante", numeroIdentificacionAportante)
                    .setParameter("fechaInicioAporte", CalendarUtils.truncarHora(fechaInicioAporte))
                    .setParameter("fechaFinalAporte", CalendarUtils.truncarHoraMaxima(fechaFinalAporte)).getResultList();
            lstInconsistenciaRegistroAporteDTO.addAll(lstResultIndDep);
            lstInconsistenciaRegistroAporteDTO.addAll(lstResultPen);
        }
        return lstInconsistenciaRegistroAporteDTO;
    }

    /**
     * (non-Javadoc)
     * 
     * @see com.asopagos.bandejainconsistencias.service.PilaBandejaService#aprobarRegistrosAporteConInconsistencias(java.util.List)
     */
    @Override
    public List<InconsistenciaRegistroAporteDTO> aprobarRegistrosAporteConInconsistencias(
            List<InconsistenciaRegistroAporteDTO> lstInconsistenciaRegistroAporteDTO, UserDTO user) {
        logger.debug("Inicia PilaBandejaBusiness.aprobarRegistrosAporteConInconsistencias (List<InconsistenciaRegistroAporteDTO>)");
        List<InconsistenciaRegistroAporteDTO> resultInconsistenciasRegAporteDTO = new ArrayList<InconsistenciaRegistroAporteDTO>();
        PilaArchivoIRegistro2 registroAporteIndDep = null;
        PilaArchivoIPRegistro2 registroAportePen = null;
        for (InconsistenciaRegistroAporteDTO inconsistenciaRegistroAporteDTO : lstInconsistenciaRegistroAporteDTO) {
            if (inconsistenciaRegistroAporteDTO.getIdIndicePlanilla() == null) {
                // error no esta el indice planilla
                inconsistenciaRegistroAporteDTO.setRegistroProcesado(false);
                break;
            }
            else if (inconsistenciaRegistroAporteDTO.getIdAporteIndDepRegistro2() == null
                    && inconsistenciaRegistroAporteDTO.getIdAportePenRegistro2() == null) {
                // error registro no valido sin identificador para busqueda
                inconsistenciaRegistroAporteDTO.setRegistroProcesado(false);
                break;
            }
            else if (inconsistenciaRegistroAporteDTO.getIdAporteIndDepRegistro2() != null) {
                // se realiza proceso de aprobacion del registro del dependiente
                // e independiente
                try {
                    registroAporteIndDep = entityManager
                            .createNamedQuery(NamedQueriesConstants.CONSULTAR_REGISTRO_APORTE_CON_INCONSISTENCIA_IND_DEP,
                                    PilaArchivoIRegistro2.class)
                            .setParameter("idRegistroAporte", inconsistenciaRegistroAporteDTO.getIdAporteIndDepRegistro2())
                            .getSingleResult();
                } catch (NoResultException e) {
                    // No se encuentra el registro de aporte de planilla PILA
                    // correspondiente
                    inconsistenciaRegistroAporteDTO.setRegistroProcesado(false);
                    break;
                }
                switch (inconsistenciaRegistroAporteDTO.getEstadoAporte()) {
                    case NO_OK:
                        //registroAporteIndDep.setEstadoRegistroAporte(EstadoRegistroAportesArchivoEnum.NO_OK_APROBADO);
                        // TODO crear campos en registro de aporte de planilla PILA,
                        // definidos en la hu399
                        // registroAporteIndDep.setUsuarioAprobador();
                        // registroAporteIndDep.setNumeroOperacionAprobacion(1234L);
                        break;
                    case NO_VALIDADO_BD:
                        //registroAporteIndDep.setEstadoRegistroAporte(EstadoRegistroAportesArchivoEnum.NO_VALIDADO_BD_APROBADO);
                        // TODO crear campos en registro de aporte de planilla PILA,
                        // definidos en la hu399
                        // registroAporteIndDep.setUsuarioAprobador();
                        // registroAporteIndDep.setNumeroOperacionAprobacion(1234L);
                        break;
                    default:
                        inconsistenciaRegistroAporteDTO.setRegistroProcesado(false);
                        break;
                }
                if (!inconsistenciaRegistroAporteDTO.getRegistroProcesado()) {
                    // TODO ERROR ESTADO NO VALIDO
                    break;
                }
                //entityManager.merge(registroAporteIndDep);
                // TODO verificar estado de archivo con relacion a la aprobacion
                // de los registros de aporte manual
                inconsistenciaRegistroAporteDTO.setRegistroProcesado(true);
                break;
            }
            else if (inconsistenciaRegistroAporteDTO.getIdAportePenRegistro2() != null) {
                // se realiza proceso de aprobacion del registro del pensionado
                try {
                    registroAportePen = entityManager
                            .createNamedQuery(NamedQueriesConstants.CONSULTAR_REGISTRO_APORTE_CON_INCONSISTENCIA_PEN,
                                    PilaArchivoIPRegistro2.class)
                            .setParameter("idRegistroAporte", inconsistenciaRegistroAporteDTO.getIdAportePenRegistro2()).getSingleResult();
                } catch (NoResultException e) {
                    // logger.debug("No se encuentra el registro de aporte de
                    // planilla PILA correspondiente");
                    inconsistenciaRegistroAporteDTO.setRegistroProcesado(false);
                    break;
                }
                switch (inconsistenciaRegistroAporteDTO.getEstadoAporte()) {
                    case NO_OK:
                        //registroAportePen.setEstadoRegistroAporte(EstadoRegistroAportesArchivoEnum.NO_OK_APROBADO);
                        //registroAportePen.setFechaProcesamientoValidRegAporte(new Date());
                        // TODO crear campos en registro de aporte de planilla PILA,
                        // definidos en la hu399
                        // registroAportePen.setUsuarioAprobador();
                        // registroAportePen.setNumeroOperacionAprobacion(1234L);
                        break;
                    case NO_VALIDADO_BD:
                        //registroAportePen.setEstadoRegistroAporte(EstadoRegistroAportesArchivoEnum.NO_VALIDADO_BD_APROBADO);
                        // TODO crear campos en registro de aporte de planilla PILA,
                        // definidos en la hu399
                        // registroAportePen.setUsuarioAprobador();
                        // registroAportePen.setNumeroOperacionAprobacion(1234L);
                        break;
                    default:
                        inconsistenciaRegistroAporteDTO.setRegistroProcesado(false);
                        break;
                }
                if (!inconsistenciaRegistroAporteDTO.getRegistroProcesado()) {
                    // TODO ERROR ESTADO NO VALIDO
                    break;
                }
                entityManager.merge(registroAportePen);
                // TODO verificar estado de archivo con relacion a la aprobacion
                // de los registros de aporte manual
                inconsistenciaRegistroAporteDTO.setRegistroProcesado(true);
                break;
            }
        }
        if (!lstInconsistenciaRegistroAporteDTO.isEmpty()) {
            logger.debug("Finaliza PilaBandejaBusiness.aprobarRegistrosAporteConInconsistencias (List<InconsistenciaRegistroAporteDTO>)");
            return lstInconsistenciaRegistroAporteDTO;
        }
        else {
            logger.debug("Finaliza PilaBandejaBusiness.aprobarRegistrosAporteConInconsistencias (List<InconsistenciaRegistroAporteDTO>)");
            throw new TechnicalException(MensajesGeneralConstants.ERROR_TECNICO_INESPERADO);
        }
    }

    // HU-389
    /**
     * (non-Javadoc)
     * 
     * @see com.asopagos.pila.composite.service.PilaCompositeService#buscarControlResultadosPersona(com.asopagos.entidades.ccf.personas.Persona,
     *      java.lang.Long, java.lang.Long,
     *      com.asopagos.rest.security.dto.UserDTO)
     */
    @Override
    public List<RespuestaConsultaEmpleadorDTO> buscarControlResultadosPersona(TipoIdentificacionEnum tipoDocumento, String idAportante,
            Long numeroPlanilla, Long periodo, UserDTO userDTO) {

        // TODO Auto-generated method stub
        
        // To ta malo muchacho, muchacho

        logger.debug(
                "Inicia buscarControlResultadosEmpleador(Empleador empleador, Long numeroPlanilla, Integer añoPeriodo,Integer mesPeriodo, UserDTO userDTO)");
        List<Object[]> consulta = new ArrayList<Object[]>();
        List<Object[]> ConsultaIP = new ArrayList<Object[]>();
        List<RespuestaConsultaEmpleadorDTO> result = new ArrayList<RespuestaConsultaEmpleadorDTO>();
        String periodoAporte = "";
        // Si se establece periodo en la solicitud se realiza casting para
        // obtener el año y el mes

        if (periodo != null) {

            Date fechaPeriodo = new Date(periodo);
            Calendar calendar = new GregorianCalendar();
            calendar.setTime(fechaPeriodo);
            Integer anio = calendar.get(Calendar.YEAR);
            Integer mes = calendar.get(Calendar.MONTH) + 1;

            periodoAporte = "" + anio + "-" + mes;

        }
        if (tipoDocumento != null && idAportante != null) {

            if (numeroPlanilla != null) {

                if (periodo != null) {
                    // consulta con todos los atributos de busqueda

                    logger.debug("Inicia Consulta con Persona,numero de planilla y periodo");

                    logger.debug("Finaliza Consulta con empleador,numero de planilla y periodo");

                    try {
                        logger.debug("Inicia Consulta con empleador,numero de planilla y periodo");
                        consulta = entityManager.createNamedQuery(NamedQueriesConstants.OBTENER_PLANILLAS_PERSONA_I_TODOS_ARGUMENTOS)
                                .setParameter("tipoDocumento", tipoDocumento.getValorEnPILA()).setParameter("idAportante", idAportante)
                                .setParameter("periodoAporte", periodoAporte).setParameter("numeroPlanilla", numeroPlanilla)
                                .getResultList();
                        result.addAll(mapeoAportesEmpresa(consulta));
                        // Mapeo Datos
                        ConsultaIP = entityManager.createNamedQuery(NamedQueriesConstants.OBTENER_PLANILLAS_PERSONA_IP_TODOS_ARGUMENTOS)
                                .setParameter("tipoDocumento", tipoDocumento.getValorEnPILA()).setParameter("idAportante", idAportante)
                                .setParameter("periodoAporte", periodoAporte).setParameter("numeroPlanilla", numeroPlanilla)
                                .getResultList();
                        result.addAll(mapeoAportesEmpresaPensionados(ConsultaIP));
                        // Mapeo Datos
                        logger.debug("Finaliza Consulta con empleador,numero de planilla y periodo");
                        return calcularCantidadTotalAportes(result);
                    } catch (Exception e) {
                        logger.error("Error al realizar la consulta,verifique los datos", e);
                        logger.debug("Finaliza Consulta con empleador,numero de planilla y periodo");
                    }

                }
                else {
                    // consulta sin periodo
                    try {
                        logger.debug("Inicia Consulta con empleador,numero de planilla");
                        consulta = entityManager.createNamedQuery(NamedQueriesConstants.OBTENER_PLANILLAS_PERSONA_I_PLANILLA)
                                .setParameter("tipoDocumento", tipoDocumento.getValorEnPILA()).setParameter("idAportante", idAportante)
                                .setParameter("numeroPlanilla", numeroPlanilla).getResultList();
                        result.addAll(mapeoAportesEmpresa(consulta));
                        // Mapeo Datos
                        ConsultaIP = entityManager.createNamedQuery(NamedQueriesConstants.OBTENER_PLANILLAS_PERSONA_IP_PLANILLA)
                                .setParameter("tipoDocumento", tipoDocumento.getValorEnPILA()).setParameter("idAportante", idAportante)
                                .setParameter("numeroPlanilla", numeroPlanilla).getResultList();
                        result.addAll(mapeoAportesEmpresaPensionados(ConsultaIP));
                        // Mapeo Datos
                        logger.debug("Fin Consulta con empleador,numero de planilla");
                        return calcularCantidadTotalAportes(result);
                    } catch (Exception e) {
                        logger.error("Error al realizar la consulta,verifique los datos", e);
                        logger.debug("Fin Consulta con empleador,numero de planilla");
                    }

                }
            }
            if (periodo != null) {
                // consulta empleador y periodo
                try {

                    logger.debug("Consulta con empleador y periodo");

                    consulta = entityManager.createNamedQuery(NamedQueriesConstants.OBTENER_PLANILLAS_PERSONA_I_PERIODO)
                            .setParameter("tipoDocumento", tipoDocumento.getValorEnPILA()).setParameter("idAportante", idAportante)
                            .setParameter("periodoAporte", periodoAporte).getResultList();
                    result.addAll(mapeoAportesEmpresa(consulta));
                    // Mapeo Datos
                    ConsultaIP = entityManager.createNamedQuery(NamedQueriesConstants.OBTENER_PLANILLAS_PERSONA_IP_PERIODO)
                            .setParameter("tipoDocumento", tipoDocumento.getValorEnPILA()).setParameter("idAportante", idAportante)
                            .setParameter("periodoAporte", periodoAporte).getResultList();
                    result.addAll(mapeoAportesEmpresaPensionados(ConsultaIP));
                    // Mapeo Datos
                    logger.debug("Finaliza Consulta con empleador y periodo");
                    return calcularCantidadTotalAportes(result);
                } catch (Exception e) {
                    logger.error("Error al realizar la consulta,verifique los datos", e);
                    logger.debug("Finaliza Consulta con empleador y periodo");
                }
            }

        }
        // No existe Persona
        if (numeroPlanilla != null) {
            if (periodo != null) {
                // consulta con numero de planilla y periodo
                try {
                    logger.debug("consulta con numero de planilla y periodo");
                    consulta = entityManager.createNamedQuery(NamedQueriesConstants.OBTENER_PLANILLAS_I_PERIODO_PLANILLA)
                            .setParameter("periodoAporte", periodoAporte).setParameter("numeroPlanilla", numeroPlanilla).getResultList();
                    result.addAll(mapeoAportesEmpresa(consulta));
                    // Mapeo Datos
                    ConsultaIP = entityManager.createNamedQuery(NamedQueriesConstants.OBTENER_PLANILLAS_IP_PERIODO_PLANILLA)
                            .setParameter("periodoAporte", periodoAporte).setParameter("numeroPlanilla", numeroPlanilla).getResultList();
                    result.addAll(mapeoAportesEmpresaPensionados(ConsultaIP));
                    // Mapeo Datos
                    logger.debug("Finaliza consulta con numero de planilla y periodo");
                    return calcularCantidadTotalAportes(result);
                } catch (Exception e) {
                    logger.error("Error al realizar la consulta,verifique los datos", e);
                    logger.debug("Finaliza consulta con numero de planilla y periodo");
                }

            }
            else {
                // consulta con numero de planilla
                try {
                    logger.debug("consulta con numero de planilla ");
                    consulta = entityManager.createNamedQuery(NamedQueriesConstants.OBTENER_PLANILLAS_I_PLANILLA)
                            .setParameter("numeroPlanilla", numeroPlanilla).getResultList();
                    result.addAll(mapeoAportesEmpresa(consulta));
                    // Mapeo Datos
                    ConsultaIP = entityManager.createNamedQuery(NamedQueriesConstants.OBTENER_PLANILLAS_IP_PLANILLA)
                            .setParameter("numeroPlanilla", numeroPlanilla).getResultList();
                    // Mapeo Datos
                    result.addAll(mapeoAportesEmpresaPensionados(ConsultaIP));
                    logger.debug("Finaliza consulta con numero de planilla ");
                    return calcularCantidadTotalAportes(result);
                } catch (Exception e) {
                    logger.error("Error al realizar la consulta,verifique los datos", e);
                    logger.debug("Finaliza consulta con numero de planilla ");
                }

            }
        }

        return null;
    }

    /**
     * (non-Javadoc)
     * 
     * @see com.asopagos.pila.composite.service.PilaCompositeService#buscarControlResultadosEmpleador(com.asopagos.entidades.ccf.personas.Empleador,
     *      java.lang.Long, java.lang.Long,
     *      com.asopagos.rest.security.dto.UserDTO)
     */
    @Override
    public List<RespuestaConsultaEmpleadorDTO> buscarControlResultadosEmpleador(TipoIdentificacionEnum tipoDocumento, String idAportante,
            Long numeroPlanilla, Long periodo, UserDTO userDTO) {

        // TODO Auto-generated method stub

        logger.debug(
                "Inicia buscarControlResultadosEmpleador(Empleador empleador, Long numeroPlanilla, Integer añoPeriodo,Integer mesPeriodo, UserDTO userDTO)");
        List<Object[]> consulta = new ArrayList<Object[]>();
        List<Object[]> ConsultaIP = new ArrayList<Object[]>();
        List<RespuestaConsultaEmpleadorDTO> result = new ArrayList<RespuestaConsultaEmpleadorDTO>();
        String periodoAporte = "";
        // Si se establece periodo en la solicitud se realiza casting para
        // obtener el año y el mes

        if (periodo != null) {

            Date fechaPeriodo = new Date(periodo);
            Calendar calendar = new GregorianCalendar();
            calendar.setTime(fechaPeriodo);
            Integer anio = calendar.get(Calendar.YEAR);
            Integer mes = calendar.get(Calendar.MONTH) + 1;

            periodoAporte = "" + anio + "-" + mes;

        }
        if (tipoDocumento != null && idAportante != null) {

            if (numeroPlanilla != null) {

                if (periodo != null) {
                    // consulta con todos los atributos de busqueda
                    try {
                        logger.debug("Inicia Consulta con empleador,numero de planilla y periodo");
                        consulta = entityManager.createNamedQuery(NamedQueriesConstants.OBTENER_PLANILLAS_EMPLEADOR_I_TODOS_ARGUMENTOS)
                                .setParameter("tipoDocumento", tipoDocumento.getValorEnPILA()).setParameter("idAportante", idAportante)
                                .setParameter("periodoAporte", periodoAporte).setParameter("numeroPlanilla", numeroPlanilla)
                                .getResultList();

                        // Mapeo Datos
                        result.addAll(mapeoAportesEmpresa(consulta));
                        ConsultaIP = entityManager.createNamedQuery(NamedQueriesConstants.OBTENER_PLANILLAS_EMPLEADOR_IP_TODOS_ARGUMENTOS)
                                .setParameter("tipoDocumento", tipoDocumento.getValorEnPILA()).setParameter("idAportante", idAportante)
                                .setParameter("periodoAporte", periodoAporte).setParameter("numeroPlanilla", numeroPlanilla)
                                .getResultList();
                        // Mapeo Datos
                        result.addAll(mapeoAportesEmpresaPensionados(ConsultaIP));
                        logger.debug("Finaliza Consulta con empleador,numero de planilla y periodo");
                        return calcularCantidadTotalAportes(result);
                    } catch (Exception e) {
                        // TODO: handle exception
                        logger.error("Error al realizar la consulta,verifique los datos", e);
                        logger.debug("Finaliza Consulta con empleador,numero de planilla y periodo");
                    }

                }
                else {
                    // consulta sin periodo
                    try {
                        logger.debug("Inicia Consulta con empleador,numero de planilla");
                        consulta = entityManager.createNamedQuery(NamedQueriesConstants.OBTENER_PLANILLAS_EMPLEADOR_I_PLANILLA)
                                .setParameter("tipoDocumento", tipoDocumento.getValorEnPILA()).setParameter("idAportante", idAportante)
                                .setParameter("numeroPlanilla", numeroPlanilla).getResultList();

                        // Mapeo Datos
                        result.addAll(mapeoAportesEmpresa(consulta));
                        ConsultaIP = entityManager.createNamedQuery(NamedQueriesConstants.OBTENER_PLANILLAS_EMPLEADOR_IP_PLANILLA)
                                .setParameter("tipoDocumento", tipoDocumento.getValorEnPILA()).setParameter("idAportante", idAportante)
                                .setParameter("numeroPlanilla", numeroPlanilla).getResultList();
                        // Mapeo Datos
                        result.addAll(mapeoAportesEmpresaPensionados(ConsultaIP));
                        logger.debug("Fin Consulta con empleador,numero de planilla");
                        return calcularCantidadTotalAportes(result);
                    } catch (Exception e) {
                        logger.error("Error al realizar la consulta,verifique los datos", e);
                        logger.debug("Fin Consulta con empleador,numero de planilla");
                    }

                }
            }
            if (periodo != null) {
                // consulta empleador y periodo
                try {

                    logger.debug("Consulta con empleador y periodo");

                    consulta = entityManager.createNamedQuery(NamedQueriesConstants.OBTENER_PLANILLAS_EMPLEADOR_I_PERIODO)
                            .setParameter("tipoDocumento", tipoDocumento.getValorEnPILA()).setParameter("idAportante", idAportante)
                            .setParameter("periodoAporte", periodoAporte).getResultList();

                    // Mapeo Datos
                    result.addAll(mapeoAportesEmpresa(consulta));
                    ConsultaIP = entityManager.createNamedQuery(NamedQueriesConstants.OBTENER_PLANILLAS_EMPLEADOR_IP_PERIODO)
                            .setParameter("tipoDocumento", tipoDocumento.getValorEnPILA()).setParameter("idAportante", idAportante)
                            .setParameter("periodoAporte", periodoAporte).getResultList();
                    // Mapeo Datos
                    result.addAll(mapeoAportesEmpresaPensionados(ConsultaIP));
                    logger.debug("Finaliza Consulta con empleador y periodo");
                    return calcularCantidadTotalAportes(result);
                } catch (Exception e) {
                    logger.error("Error al realizar la consulta,verifique los datos", e);
                    logger.debug("Finaliza Consulta con empleador y periodo");
                }
            }

        }
        // No existe empleador
        if (numeroPlanilla != null) {
            if (periodo != null) {
                // consulta con numero de planilla y periodo
                try {
                    logger.debug("consulta con numero de planilla y periodo");
                    consulta = entityManager.createNamedQuery(NamedQueriesConstants.OBTENER_PLANILLAS_I_PERIODO_PLANILLA)
                            .setParameter("periodoAporte", periodoAporte).setParameter("numeroPlanilla", numeroPlanilla).getResultList();

                    // Mapeo Datos
                    result.addAll(mapeoAportesEmpresa(consulta));
                    ConsultaIP = entityManager.createNamedQuery(NamedQueriesConstants.OBTENER_PLANILLAS_IP_PERIODO_PLANILLA)
                            .setParameter("periodoAporte", periodoAporte).setParameter("numeroPlanilla", numeroPlanilla).getResultList();
                    // Mapeo Datos
                    result.addAll(mapeoAportesEmpresaPensionados(ConsultaIP));
                    logger.debug("Finaliza consulta con numero de planilla y periodo");
                    return calcularCantidadTotalAportes(result);
                } catch (Exception e) {
                    logger.error("Error al realizar la consulta,verifique los datos", e);
                    logger.debug("Finaliza consulta con numero de planilla y periodo");
                }

            }
            else {
                // consulta con numero de planilla
                try {
                    logger.debug("consulta con numero de planilla ");
                    consulta = entityManager.createNamedQuery(NamedQueriesConstants.OBTENER_PLANILLAS_I_PLANILLA)
                            .setParameter("numeroPlanilla", numeroPlanilla).getResultList();
                    result.addAll(mapeoAportesEmpresa(consulta));

                    // Mapeo Datos
                    ConsultaIP = entityManager.createNamedQuery(NamedQueriesConstants.OBTENER_PLANILLAS_IP_PLANILLA)
                            .setParameter("numeroPlanilla", numeroPlanilla).getResultList();
                    // Mapeo Datos
                    result.addAll(mapeoAportesEmpresaPensionados(ConsultaIP));

                    logger.debug("Finaliza consulta con numero de planilla ");
                    return calcularCantidadTotalAportes(result);
                } catch (Exception e) {
                    logger.error("Error al realizar la consulta,verifique los datos", e);
                    logger.debug("Finaliza consulta con numero de planilla ");
                }

            }
        }

        return null;
    }

    /**
     * Metodo que calcula la cantidad y el total de aportes de una lista
     * 
     * @param registrosAportes
     *        List<code>RespuestaConsultaEmpleadorDTO</code> contiene los
     *        registros con la cabecera del aportante y una lista de
     *        cotizantes
     * @return registrosAportes List<code>RespuestaConsultaEmpleadorDTO</code>
     *         Lista con los datos establecidos
     */
    public List<RespuestaConsultaEmpleadorDTO> calcularCantidadTotalAportes(List<RespuestaConsultaEmpleadorDTO> registrosAportes) {
        if (registrosAportes.isEmpty()) {
            return null;
        }
        else {
            Integer totalAportes = 0;
            Integer cantidadAportes = 0;
            for (RespuestaConsultaEmpleadorDTO respuesta : registrosAportes) {
                for (DetalleTablaAportanteDTO detalle : respuesta.getRegistros()) {
                    totalAportes += detalle.getAporteObligatorio();
                    cantidadAportes += 1;
                }
                respuesta.setCantidadAportes(cantidadAportes);
                respuesta.setTotalAportes(totalAportes);
                totalAportes = 0;
                cantidadAportes = 0;

            }
            return registrosAportes;
        }
    }
    
    /* (non-Javadoc)
     * @see com.asopagos.bandejainconsistencias.service.PilaBandejaService#consultarEmpPendientesPorAfiliar(java.lang.String, com.asopagos.enumeraciones.personas.TipoIdentificacionEnum, java.lang.Short, java.lang.Long)
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<EmpAporPendientesPorAfiliarDTO> consultarEmpPendientesPorAfiliar(String numeroIdentificacion,
            TipoIdentificacionEnum tipoIdentificacion, Short digitoVerificacion, Long fechaIngresoBandeja, UriInfo uri,
            HttpServletResponse response) {
        logger.debug("Inicia consultarEmpPendientesPorAfiliar(String, TipoIdentificacionEnum, Short, "
                + "Long, UriInfo, HttpServletResponse)");

        List<EmpAporPendientesPorAfiliarDTO> empleadoresPorAfiliar = new ArrayList<EmpAporPendientesPorAfiliarDTO>();
        
        //Consulta
        Query query = null;
        
        //QueryBuilder
        QueryBuilder querybuilder = new QueryBuilder(entityManagerCore, uri, response);
        querybuilder.addOrderByDefaultParam("-fechaUltimoRecaudoAporte");
        
        // No llegan parametros, buscar todos
        if (numeroIdentificacion == null && tipoIdentificacion == null 
                && digitoVerificacion == null && fechaIngresoBandeja == null){
            //query = querybuilder.createQuery(NamedQueriesConstants.BUSQUEDA_APORTANTES_PENDIENTES_POR_AFILIAR, null);
            query = entityManagerCore.createNamedQuery(NamedQueriesConstants.BUSQUEDA_APORTANTES_PENDIENTES_POR_AFILIAR, null);
        }
        
        //Llega solo la fecha
        if(numeroIdentificacion == null && tipoIdentificacion == null 
                && digitoVerificacion == null && fechaIngresoBandeja != null ){
            Date fecEntradaBandeja = CalendarUtils.truncarHora(new Date(fechaIngresoBandeja));
            //querybuilder.addParam("fechaEntradaBandeja", fecEntradaBandeja);
            //query = querybuilder.createQuery(NamedQueriesConstants.BUSQUEDA_APORTANTES_PENDIENTES_POR_AFILIAR_FECHA, null);
            // TODO Le fecha de la consulta esta con fecha de retiro porque no se ha definido fecha de ingreso a bandeja
            query = entityManagerCore.createNamedQuery(NamedQueriesConstants.BUSQUEDA_APORTANTES_PENDIENTES_POR_AFILIAR_FECHA, null);
            query.setParameter("fechaEntradaBandeja", fecEntradaBandeja);
        }
        
        // Llega NIT, TipoID, DV, FechaIngresoBandeja, 
        if (numeroIdentificacion != null && tipoIdentificacion != null 
                && digitoVerificacion != null && fechaIngresoBandeja != null 
                && TipoIdentificacionEnum.NIT.equals(tipoIdentificacion)){
            Date fecEntradaBandeja = CalendarUtils.truncarHora(new Date(fechaIngresoBandeja));
//            querybuilder.addParam("fechaEntradaBandeja", fecEntradaBandeja);
//            querybuilder.addParam("numeroIdentificacion", numeroIdentificacion);
//            querybuilder.addParam("tipoIdentificacion", tipoIdentificacion);
//            querybuilder.addParam("digitoVerificacion", digitoVerificacion);
//            query = querybuilder.createQuery(NamedQueriesConstants.BUSQUEDA_APORTANTES_PENDIENTES_POR_AFILIAR_NITFECHA, null);
            query = entityManagerCore.createNamedQuery(NamedQueriesConstants.BUSQUEDA_APORTANTES_PENDIENTES_POR_AFILIAR_NITFECHA, null);
            query.setParameter("fechaEntradaBandeja", fecEntradaBandeja);
            query.setParameter("numeroIdentificacion",numeroIdentificacion);
            query.setParameter("tipoIdentificacion", tipoIdentificacion);
            query.setParameter("digitoVerificacion", digitoVerificacion);
        }
		
		if(1==1){
			//Agregando un cambio desde el local
			doSomethingSpecial(Float f, TimeStampo ts);
			int f = 4,5f;
			int x = 4;
		}
        
        //Llegan NIT sin fecha
        if (numeroIdentificacion != null && tipoIdentificacion != null 
                && digitoVerificacion != null && fechaIngresoBandeja == null 
                && TipoIdentificacionEnum.NIT.equals(tipoIdentificacion)){
//            querybuilder.addParam("numeroIdentificacion", numeroIdentificacion);
//            querybuilder.addParam("tipoIdentificacion", tipoIdentificacion);
//            querybuilder.addParam("digitoVerificacion", digitoVerificacion);
//            query = querybuilder.createQuery(NamedQueriesConstants.BUSQUEDA_APORTANTES_PENDIENTES_POR_AFILIAR_NIT, null);
            query = entityManagerCore.createNamedQuery(NamedQueriesConstants.BUSQUEDA_APORTANTES_PENDIENTES_POR_AFILIAR_NIT, null);
            query.setParameter("numeroIdentificacion",numeroIdentificacion);
            query.setParameter("tipoIdentificacion", tipoIdentificacion);
            query.setParameter("digitoVerificacion", digitoVerificacion);
        }
 
        // Llegan todos los parametros (Tipo de documento distinto a NIT)
        if (numeroIdentificacion != null && tipoIdentificacion != null && digitoVerificacion == null
                && fechaIngresoBandeja != null && !TipoIdentificacionEnum.NIT.equals(tipoIdentificacion)){
            Date fecEntradaBandeja = CalendarUtils.truncarHora(new Date(fechaIngresoBandeja));
//            querybuilder.addParam("fechaEntradaBandeja", fecEntradaBandeja);
//            querybuilder.addParam("numeroIdentificacion", numeroIdentificacion);
//            querybuilder.addParam("tipoIdentificacion", tipoIdentificacion);            
//            query = querybuilder.createQuery(NamedQueriesConstants.BUSQUEDA_APORTANTES_PENDIENTES_POR_AFILIAR_DOCFECHA, null);
            query = entityManagerCore.createNamedQuery(NamedQueriesConstants.BUSQUEDA_APORTANTES_PENDIENTES_POR_AFILIAR_DOCFECHA, null);
            query.setParameter("fechaEntradaBandeja", fecEntradaBandeja);
            query.setParameter("numeroIdentificacion",numeroIdentificacion);
            query.setParameter("tipoIdentificacion", tipoIdentificacion);            
        }
        
        // Llegan documento y tipo documento
        if (numeroIdentificacion != null && tipoIdentificacion != null && digitoVerificacion == null
                && fechaIngresoBandeja == null && !TipoIdentificacionEnum.NIT.equals(tipoIdentificacion)){
//            querybuilder.addParam("numeroIdentificacion", numeroIdentificacion);
//            querybuilder.addParam("tipoIdentificacion", tipoIdentificacion);            
//            query = querybuilder.createQuery(NamedQueriesConstants.BUSQUEDA_APORTANTES_PENDIENTES_POR_AFILIAR_DOC, null);
            query = entityManagerCore.createNamedQuery(NamedQueriesConstants.BUSQUEDA_APORTANTES_PENDIENTES_POR_AFILIAR_DOC, null);
            query.setParameter("numeroIdentificacion",numeroIdentificacion);
            query.setParameter("tipoIdentificacion", tipoIdentificacion);            
        }        

       if(query != null){
           empleadoresPorAfiliar = (List<EmpAporPendientesPorAfiliarDTO>) query.getResultList();
        }
        
        logger.debug("Finaliza consultarEmpPendientesPorAfiliar(String, TipoIdentificacionEnum, Short, "
                + "Long, UriInfo, HttpServletResponse)");
        
        return empleadoresPorAfiliar;
    }

    /* (non-Javadoc)
     * @see com.asopagos.bandejainconsistencias.service.PilaBandejaService#consultarEmpCeroTrabajadoresActivos(java.lang.String, com.asopagos.enumeraciones.personas.TipoIdentificacionEnum, java.lang.String, java.lang.Short, java.lang.Long, java.lang.Long, java.lang.Boolean, javax.ws.rs.core.UriInfo, javax.servlet.http.HttpServletResponse)
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<EmpCeroTrabajadoresActivosDTO> consultarEmpCeroTrabajadoresActivos(String numeroIdentificacion,
            TipoIdentificacionEnum tipoIdentificacion, String nombreEmpresa, Short digitoVerificacion, Long fechaInicioIngresoBandeja,
            Long fechaFinIngresoBandeja, Boolean empGestionado, UriInfo uri, HttpServletResponse response) {
        
        logger.debug("Inicia consultarEmpCeroTrabajadoresActivos(String numeroIdentificacion, "
                + "TipoIdentificacionEnum tipoIdentificacion, String nombreEmpresa, Short digitoVerificacion, Long fechaInicioIngresoBandeja, "
                + "Long fechaFinIngresoBandeja, UriInfo uri, HttpServletResponse response)");
        
        // TODO Esperar lo que definan para la 404
        
        logger.debug("Finaliza consultarEmpCeroTrabajadoresActivos(String numeroIdentificacion, "
                + "TipoIdentificacionEnum tipoIdentificacion, String nombreEmpresa, Short digitoVerificacion, Long fechaInicioIngresoBandeja, "
                + "Long fechaFinIngresoBandeja, UriInfo uri, HttpServletResponse response)");
        
        return null;
    }

    @Override
    public void actualizarRegistroVigenteBandejaEmpCeroTrabajadoresActivos(List<Long> idRegistros, UserDTO user) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void guardarRegistroBandejaEmpCeroTrabajadoresActivos(List<EmpCeroTrabajadoresActivosDTO> empleadores, UserDTO user) {
        logger.debug("Inicia guardarRegistroBandejaEmpCeroTrabajadoresActivos(List<EmpCeroTrabajadoresActivosDTO>, UserDTO user)");
        // TODO 
    }
    
    //actualizar RegistroAnteriorBandejaEp idReg set registroVigente = false;
    
    //guardar guardarempCeroBandeja insert con el numero de radicado y el usuario que viene pro contexto @Context UserDTO userDTO; registrovigente = true;
	
	public static void anotherDamnMethod(String a, String b){
		//Consulta 
		empleadoresCero = entityManagerCore.createNamedQuery(NamedQueriesConstants.BUSQUEDA_EMPLEADOR_CERO_TRABAJADORES_ACTIVOS)
		    .setParameter("nombreEmpresa", nombreEmpresa) 
		    .setParameter("numeroIdentificacion", numeroIdentificacion)
		    .setParameter("tipoIdentificacion", tipoIdentificacion)
		    .setParameter("digitoVerificacion", digitoVerificacion)
		    .setParameter("fechaInicioIngresoBandeja", fecIniEntradaBandeja)
		    .setParameter("fechaFinIngresoBandeja", fecFinEntradaBandeja).getResultList();

		
		// Cuando no llegan parametros
		public static final String BUSQUEDA_EMPLEADOR_CERO_TRABAJADORES_ACTIVOS = "PilaBandejaService.Empleador.BusquedaEmpleadorCeroTrabajadoresActivos";
		// Busqueda de los RolAfiliado que han sido retirados por PILA
		public static final String BUSQUEDA_ROL_AFILIADO_RETIRADO_POR_PILA = "PilaBandejaService.Empleador.BusquedaRolAfiliadoRetiradoPorPila";
		// Actualizar la fecha de gestion del empleador
		public static final String ACTUALIZAR_FECHA_GESTION_EMPLEADOR= "PilaBandejaService.Empleador.ActualizarFechaGestionEmpleador";
	}
    
}
