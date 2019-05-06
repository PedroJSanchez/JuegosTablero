/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package smma.juegosTablero.agentes;

import jade.content.Concept;
import jade.content.ContentManager;
import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.BeanOntologyException;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFSubscriber;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.proto.ProposeInitiator;
import jade.proto.SubscriptionInitiator;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import juegosTablero.Vocabulario;
import static juegosTablero.Vocabulario.ModoJuego.TORNEO;
import static juegosTablero.Vocabulario.ModoJuego.UNICO;
import static juegosTablero.Vocabulario.NombreServicio.GRUPO_JUEGOS;
import static juegosTablero.Vocabulario.TipoJuego.DOMINO;
import juegosTablero.aplicacion.barcos.JuegoBarcos;
import juegosTablero.aplicacion.conecta4.JuegoConecta4;
import juegosTablero.aplicacion.domino.JuegoDomino;
import juegosTablero.dominio.elementos.ClasificacionJuego;
import juegosTablero.dominio.elementos.Informe;
import juegosTablero.dominio.elementos.Gestor;
import juegosTablero.dominio.elementos.Juego;
import juegosTablero.dominio.elementos.JuegoAceptado;
import juegosTablero.dominio.elementos.Jugador;
import juegosTablero.dominio.elementos.Motivacion;
import juegosTablero.dominio.elementos.ProponerJuego;
import smma.juegosTablero.gui.Consola;
import smma.juegosTablero.gui.JuegosTableroJFrame;
import smma.juegosTablero.Constantes;
import static smma.juegosTablero.Constantes.ENFRENTAMIENTO.MEJOR_UNO;
import static juegosTablero.Vocabulario.getOntologia;
import juegosTablero.dominio.elementos.CompletarJuego;
import juegosTablero.dominio.elementos.DetalleInforme;
import juegosTablero.dominio.elementos.Grupo;
import juegosTablero.dominio.elementos.InformarJuego;
import smma.juegosTablero.gui.AgentesJuegoJFrame;
import smma.juegosTablero.gui.ClasificacionJuegoJFrame;
import smma.juegosTablero.util.RegistroGrupoJuegos;
import smma.juegosTablero.util.RegistroJuego;
import smma.juegosTablero.util.RegistroJugador;

/**
 *
 * @author pedroj
 */
public class AgenteCentralJuego extends Agent implements Constantes, Vocabulario {

    // Generador de números aleatorios
    private final Random aleatorio = new Random();

    // Para la generación y obtención del contenido de los mensages
    private ContentManager[] manager;

    // El lenguaje utilizado por el agente para la comunicación es SL 
    private final Codec codec = new SLCodec();

    // Las ontología que utilizará el agente
    private Ontology[] listaOntologias;

    // Servicios y juegos
    private NombreServicio[] tiposAgentes;
    private TipoJuego[] listaJuegos;

    // Variables agente
    private int numJuego;
    private Gestor agenteCentralJuego;
    private List<AID>[] listaAgentes;
    private List<RegistroGrupoJuegos>[] registroGrupoJuegos;
    private List<RegistroJugador>[] registroJugadores;
    private Map<String, RegistroJuego> registroJuegos;
    private int[] minJugadores;
    private List<TipoJuego> tipoJuegosActivos;

    // GUI del agente
    private Consola guiConsola;
    private JuegosTableroJFrame guiAgente;
    private AgentesJuegoJFrame guiAgentesJuego;

    @Override
    protected void setup() {
        // Inicialización de las variables
        numJuego = 0;
        agenteCentralJuego = new Gestor("Agente Central Juego", this.getAID());
        guiConsola = new Consola(this);
        guiAgente = new JuegosTableroJFrame(this);
        guiConsola.mensaje("Comienza la ejecución " + agenteCentralJuego);
        guiAgentesJuego = new AgentesJuegoJFrame(this);

        tiposAgentes = NombreServicio.values();
        listaJuegos = TipoJuego.values();
        tipoJuegosActivos = new ArrayList();
        registroJuegos = new HashMap();

        // Tipos de Agentes
        listaAgentes = new List[tiposAgentes.length];
        for (NombreServicio servicio : tiposAgentes) {
            listaAgentes[servicio.ordinal()] = new ArrayList();
        }

        registroGrupoJuegos = new List[listaJuegos.length];
        registroJugadores = new List[listaJuegos.length];
        minJugadores = new int[listaJuegos.length];
        for (TipoJuego tipo : listaJuegos) {
            registroGrupoJuegos[tipo.ordinal()] = new ArrayList();
            registroJugadores[tipo.ordinal()] = new ArrayList();
            if (tipo.equals(DOMINO)) {
                minJugadores[tipo.ordinal()] = CUATRO_JUGADORES;
            } else {
                minJugadores[tipo.ordinal()] = DOS_JUGADORES;
            }
        }

        // Registro de las ontologías para los juegos de tablero
        listaOntologias = new Ontology[listaJuegos.length];
        manager = new ContentManager[listaJuegos.length];
        try {
            for (int i = 0; i < listaJuegos.length; i++) {
                listaOntologias[i] = getOntologia(listaJuegos[i]);
                manager[i] = (ContentManager) getContentManager();
                manager[i].registerLanguage(codec);
                manager[i].registerOntology(listaOntologias[i]);
            }
        } catch (BeanOntologyException ex) {
            guiConsola.mensaje("Error al registrar la ontología \n" + ex);
            this.doDelete();
        }

        // Añadir tareas principales
        // Suscripción a las páginas amarillas para saber los agentes del juego
        // que se registran o finalizan el registro
        DFAgentDescription template = new DFAgentDescription();
	ServiceDescription templateSd = new ServiceDescription();
        templateSd.setType(TIPO_SERVICIO);
        template.addServices(templateSd);
        addBehaviour(new TareaSuscripcionDF(this,template)); 
        
    }

    @Override
    protected void takeDown() {
        // Liberación de los recursos del agente
        guiConsola.dispose();
        guiAgente.dispose();
        guiAgentesJuego.dispose();

        System.out.println("Finaliza la ejecución de " + this.getName());
    }

    private void addAgentesJugador(ACLMessage msg, Juego juego, int numAgentes) {
        String idJuego = juego.getIdJuego();
        TipoJuego tipoJuego = juego.getTipoJuego();
        ModoJuego modo = juego.getModoJuego();
        
        if ( registroJuegos.containsKey(idJuego) ) {
            // El juego ya está registrado
            List listaJugadores = registroJuegos.get(idJuego).getListaJugadores();
        }
        
        if (modo.equals(TORNEO)) {
            Iterator it = listaAgentes[tipoJuego.ordinal() + 1].iterator();
            while (it.hasNext()) {
                msg.addReceiver((AID) it.next());
            }
        } else {
            for (int i = 0; i < numAgentes; i++) {
                AID jugador = listaAgentes[tipoJuego.ordinal() + 1].get(i);
                msg.addReceiver(jugador);
            }
        }
    }

    private void addRegistroJuego(ProponerJuego proponerJuego) {
        String idJuego = proponerJuego.getJuego().getIdJuego();

        if (!registroJuegos.containsKey(idJuego)) {
            registroJuegos.put(idJuego, new RegistroJuego(proponerJuego));
        }
    }

    private void addRegistroJugador(Juego juego, Jugador jugador) {
        String idJuego = juego.getIdJuego();
        TipoJuego tipoJuego = juego.getTipoJuego();
        List listaAgentes = registroJuegos.get(idJuego).getListaJugadores();

        listaAgentes.add(jugador);
        int indice = registroJugadores[tipoJuego.ordinal()].indexOf(jugador);
        if (indice == NO_HAY_ELEMENTO) {
            registroJugadores[tipoJuego.ordinal()].add(new RegistroJugador(jugador));
        } else {
            RegistroJugador registroJugador = registroJugadores[tipoJuego.ordinal()].get(indice);
            registroJugador.addJuego();
        }
    }

    private void addCentralJuegos(ACLMessage msg, Juego juego) {
        // Versión inicial
        msg.addReceiver(listaAgentes[GRUPO_JUEGOS.ordinal()].get(PRIMERO));
    }

    private void addRegistroGrupoJuegos(Grupo agenteGrupo, Juego juego) {
        TipoJuego tipoJuego = juego.getTipoJuego();
        int indice = registroGrupoJuegos[tipoJuego.ordinal()].indexOf(agenteGrupo);
        if (indice == NO_HAY_ELEMENTO) {
            registroGrupoJuegos[tipoJuego.ordinal()].add(new RegistroGrupoJuegos(agenteGrupo));
            addSuscripcion(agenteGrupo.getAgenteGrupoJuegos(),juego.getTipoJuego());
        }
        
        // Eliminamos el juego del registro pendiente para que sea completado
        registroJuegos.remove(juego.getIdJuego());
    }

    /**
     * Controla el botón para completar juegos
     */
    private void juegosPendientes() {
        if (!registroJuegos.isEmpty() && (listaAgentes[GRUPO_JUEGOS.ordinal()].size() != NO_HAY_AGENTES)) {
            guiAgente.activaCompletarJuego();
        } else {
            guiAgente.anulaCompletarJuego();
        }
    }
    
    private int buscarIndiceJuego(String nombreOntologia) {
        int resultado = NO_HAY_ELEMENTO;
        
        boolean encontrado = false;
        int indice = 0;
        while ( (indice < listaJuegos.length) && !encontrado ) {
            if ( listaOntologias[indice].getName().equals(nombreOntologia) ) {
                encontrado = true;
                resultado = indice;
            } else {
                indice++;
            } 
        }
        
        return resultado;
    }
    
    public void proponerJuego(TipoJuego tipoJuego, ModoJuego modoJuego) {
        ProponerJuego proponerJuego;
        ACLMessage msg = new ACLMessage(ACLMessage.PROPOSE);
        String idJuego = tipoJuego.name() + "_" + numJuego;
        Juego juego = null;
        
        if ( registroJuegos.containsKey(idJuego) ) {
            // El juego ya está registrado pero no tiene suficientes jugadores
            proponerJuego = registroJuegos.get(idJuego).getJuegoPropuesto();
        } else {
            // El juego no está registrado
            proponerJuego = new ProponerJuego();
            juego = new Juego(idJuego, MEJOR_UNO.victorias(), modoJuego, tipoJuego);
            proponerJuego.setJuego(juego);
            switch (tipoJuego) { // Condiciones estandar para el juego
                case BARCOS:
                    proponerJuego.setTipoJuego(new JuegoBarcos());
                    break;
                case CONECTA_4:
                    proponerJuego.setTipoJuego(new JuegoConecta4());
                    break;
                case DOMINO:
                    proponerJuego.setTipoJuego(new JuegoDomino());
                    break;
            }

            msg.setProtocol(FIPANames.InteractionProtocol.FIPA_PROPOSE);
            msg.setSender(getAID());
            msg.setLanguage(codec.getName());
            msg.setOntology(listaOntologias[tipoJuego.ordinal()].getName());
            msg.setReplyByDate(new Date(System.currentTimeMillis() + TIME_OUT));

            Action ac = new Action(this.getAID(), proponerJuego);
        
            try {
                manager[tipoJuego.ordinal()].fillContent(msg, ac);
            } catch (Codec.CodecException | OntologyException ex) {
                guiConsola.mensaje("Error en la construcción del mensaje en Proponer Juego \n" + ex);
            }

            // Registramos el juego
            
        }
        
        addRegistroJuego(proponerJuego);
        
        addAgentesJugador(msg, juego, minJugadores[tipoJuego.ordinal()]);
        
        if ( msg.getAllReceiver().hasNext() ) {
            // Si hay agentes con los que contactar
            addBehaviour(new TareaProponerJuego(this, msg, juego, minJugadores[tipoJuego.ordinal()]));
            guiConsola.mensaje(msg.toString());
        }
    }

    public void completarJuego() {
        Iterator it = registroJuegos.keySet().iterator();

        // Seleccionamos el primer juego sin completar
        if (it.hasNext()) {
            String idJuego = (String) it.next();
            RegistroJuego juegoRegistrado = registroJuegos.get(idJuego);
            Juego juego = juegoRegistrado.getJuegoPropuesto().getJuego();
            TipoJuego tipoJuego = juego.getTipoJuego();
            Concept condicionesJuego = juegoRegistrado.getJuegoPropuesto().getTipoJuego();
            List listaJugadores = juegoRegistrado.getListaJugadores();
            CompletarJuego completarJuego = new CompletarJuego(juego, condicionesJuego, 
                                                         new jade.util.leap.ArrayList((ArrayList) listaJugadores));
            
            // Creamos el mensaje
            ACLMessage msg = new ACLMessage(ACLMessage.PROPOSE);
            msg.setProtocol(FIPANames.InteractionProtocol.FIPA_PROPOSE);
            msg.setSender(getAID());
            msg.setLanguage(codec.getName());
            msg.setOntology(listaOntologias[tipoJuego.ordinal()].getName());
            addCentralJuegos(msg, juego);
            msg.setReplyByDate(new Date(System.currentTimeMillis() + TIME_OUT));

            if ( msg.getAllReceiver().hasNext() ) {
                Action ac = new Action(this.getAID(), completarJuego);
            
                try {
                    manager[tipoJuego.ordinal()].fillContent(msg, ac);
                } catch (Codec.CodecException | OntologyException ex) {
                    guiConsola.mensaje("Error en la construcción del mensaje en Completar Juego \n" + ex);
                }

                guiConsola.mensaje(msg.toString());
                addBehaviour(new TareaCompletarJuego(this, msg, juego));
            } else {
                // No hay agentes disponibles para el juego
                guiConsola.mensaje("No hay grupo de juegos posibles para completar: " + juego);
            }
        }
    }

    /**
     * Añade la suscripción para recibir las calsificaciones de los juegos que
     * completará el agente grupo de juegos.
     *
     * @param agenteGrupoJuegos
     */
    private void addSuscripcion(AID agente, TipoJuego tipo) {
        InformarJuego informarJuego = new InformarJuego(agenteCentralJuego);
        
        //Creamos el mensaje para lanzar el protocolo Subscribe
        ACLMessage msg = new ACLMessage(ACLMessage.SUBSCRIBE);
        msg.setProtocol(FIPANames.InteractionProtocol.FIPA_SUBSCRIBE);
        msg.setSender(this.getAID());
        msg.setLanguage(codec.getName());
        msg.setOntology(listaOntologias[tipo.ordinal()].getName());
        msg.addReceiver(agente);
        
        Action ac = new Action(this.getAID(), informarJuego);
        
        try {
            manager[tipo.ordinal()].fillContent(msg, ac);
        } catch (Codec.CodecException | OntologyException ex) {
            guiConsola.mensaje("Error en la construcción del mensaje en Informar Juego \n" + ex);
        }
        
        // Añadimos la tarea de suscripción
        addBehaviour(new TareaInformarJuego(this,msg));
    }
    
    /**
     * Tarea para suscripción a las páginas aparillas y que el agente DF notifique
     * el registro de un agente relacionado con los Juegos de Tablero
     */
    class TareaSuscripcionDF extends DFSubscriber {

        public TareaSuscripcionDF(Agent a, DFAgentDescription template) {
            super(a, template);
        }

        /**
         * Cada vez que se registra un agente del juego se activa y se presentará
         * en guiAgentesJuego
         * @param dfad agente que entra al registro
         */
        @Override
        public void onRegister(DFAgentDescription dfad) {
            Iterator it = dfad.getAllServices();
            while ( it.hasNext() ) {
                ServiceDescription sd = (ServiceDescription) it.next();
                for ( NombreServicio servicio : tiposAgentes ) {
                    if ( sd.getName().equals(servicio.name()) ) {
                        listaAgentes[servicio.ordinal()].add(dfad.getName());
                        guiAgentesJuego.addAgente(listaAgentes[servicio.ordinal()], servicio);
                        
                        if ( !servicio.equals(GRUPO_JUEGOS) && 
                             (listaAgentes[servicio.ordinal()].size() >= minJugadores[servicio.ordinal() - 1]) &&
                             !tipoJuegosActivos.contains(listaJuegos[servicio.ordinal() - 1]) )
                            // Juegos que tienen el mínimo de jugadores necesarios
                            tipoJuegosActivos.add(listaJuegos[servicio.ordinal() - 1]);
                        else 
                            juegosPendientes();
                        break;
                    }
                }        
            }
            
            if ( !tipoJuegosActivos.isEmpty() )
                guiAgente.activaProponerJuego(tipoJuegosActivos);   
        }

        /**
         * Cada vez que se elimina del registro un agente del juego se activa y
         * actualiza el guiAgentesJuego
         * @param dfad agente que deja el registro
         */
        @Override
        public void onDeregister(DFAgentDescription dfad) {
            for ( NombreServicio servicio : tiposAgentes ) {
                if ( listaAgentes[servicio.ordinal()].remove(dfad.getName()) ) {
                    guiAgentesJuego.addAgente(listaAgentes[servicio.ordinal()], servicio);
                        
                    if ( !servicio.equals(GRUPO_JUEGOS) && 
                        (listaAgentes[servicio.ordinal()].size() < minJugadores[servicio.ordinal() - 1]) ) {
                        // Juegos que tienen el mínimo de jugadores necesarios
                        tipoJuegosActivos.remove(listaJuegos[servicio.ordinal() - 1]);
                        
                    } else 
                        juegosPendientes();
                    break;
                }
            }
            
            if ( !tipoJuegosActivos.isEmpty() )
                guiAgente.activaProponerJuego(tipoJuegosActivos);
            else 
                guiAgente.anulaProponerJuego();   
        }
    }

    class TareaProponerJuego extends ProposeInitiator {

        private final Juego juego;
        private final int numJugadores;
        private final List<AID> agentesContactados;

        public TareaProponerJuego(Agent a, ACLMessage msg, Juego juego, int numJugadores) {
            super(a, msg);
            this.juego = juego;
            this.numJugadores = numJugadores;
            
            this.agentesContactados = new ArrayList();
            Iterator it = msg.getAllIntendedReceiver();
            while ( it.hasNext() )
                agentesContactados.add((AID)it.next());
        }

        @Override
        protected void handleOutOfSequence(ACLMessage msg) {
            // Ha llegado un mensaje fuera de la secuencia del protocolo
            guiConsola.mensaje("ERROR en Proponer Juego_____________________\n" + msg);
        }

        /**
         * Revisamos las propuestas recibidas, tanto afirmativas como negativas.
         *
         * @param responses
         */
        @Override
        protected void handleAllResponses(Vector responses) {
            String idJuego = juego.getIdJuego();
            TipoJuego tipoJuego = juego.getTipoJuego();

            Iterator it = responses.iterator();

            while (it.hasNext()) {
                ACLMessage msg = (ACLMessage) it.next();
                switch ( msg.getPerformative() ) {
                    case ACLMessage.ACCEPT_PROPOSAL:
                        try {
                            // Acepta la propuesta
                            JuegoAceptado juegoAceptado = (JuegoAceptado) manager[tipoJuego.ordinal()].extractContent(msg);
                            Jugador jugador = (Jugador) juegoAceptado.getAgenteJuego();
                            addRegistroJugador(juego, jugador);
                            guiConsola.mensaje("El agente: " + jugador.getNombre() + " acepta el juego " + juego);

                        } catch (Codec.CodecException | OntologyException ex) {
                            guiConsola.mensaje("Error en la construcción del mensaje de " + msg.getSender().getLocalName() +
                                    "\n" + msg + "\n" + ex);
                        } catch ( Exception ex ) {
                            guiConsola.mensaje("Error inesperado de " + msg.getSender().getLocalName() + "\n" + ex);
                        } break;
                    case ACLMessage.REJECT_PROPOSAL:
                        try {
                            // Rechaza la propuesta
                            Motivacion motivacion = (Motivacion) manager[tipoJuego.ordinal()].extractContent(msg);
                            guiConsola.mensaje("El agente: " + msg.getSender().getLocalName()
                                    + " rechaza el juego: " + juego + " por: " + motivacion);
                        } catch (Codec.CodecException | OntologyException ex) {
                            guiConsola.mensaje("Error en la construcción del mensaje de " + msg.getSender().getLocalName() +
                                    "\n" + msg + "\n" + ex);
                        } catch ( Exception ex ) {
                            guiConsola.mensaje("Error inesperado de " + msg.getSender().getLocalName() + "\n" + ex);
                        } break;
                    default:
                        break;
                }
            }

            RegistroJuego juegoRegistrado = registroJuegos.get(idJuego);
            if ( juegoRegistrado.numJugadores() < numJugadores) {
                completarJuego();
            } else {
                guiConsola.mensaje("Juego(" + juego.getIdJuego() + ") se ha registrado\n"
                        + registroJuegos.get(juego.getIdJuego()));
                numJuego++;
                juegosPendientes();
            }
        }

        /**
         * No hay jugadores suficientes y se repetirá la tarea hasta conseguir
         * el mínimo de jugadores. No se incluyen los que ya han contestado
         * afirmativamente
         *
         * @param juego
         */
        private void completarJuego() {
            String idJuego = juego.getIdJuego();
            TipoJuego tipoJuego = juego.getTipoJuego();
            ModoJuego modoJuego = juego.getModoJuego();
            RegistroJuego juegoPropuesto = registroJuegos.get(idJuego);
            AID jugador;

            ProponerJuego proponerJuego = juegoPropuesto.getJuegoPropuesto();
            ACLMessage msg = new ACLMessage(ACLMessage.PROPOSE);
            msg.setProtocol(FIPANames.InteractionProtocol.FIPA_PROPOSE);
            msg.setSender(myAgent.getAID());
            msg.setLanguage(codec.getName());
            msg.setOntology(listaOntologias[tipoJuego.ordinal()].getName());
            msg.setReplyByDate(new Date(System.currentTimeMillis() + TIME_OUT));

            // Añadimos a los jugadores que nos faltan para completar el juego
            List listaJugadores = juegoPropuesto.getListaJugadores();
            if (modoJuego.equals(UNICO)) {
                // Contactamos con nuevos jugadores para podere completar el juego
                // si es un juego individual
                int numAgentes = numJugadores - listaJugadores.size();
                int total = 0;
                Iterator it = listaAgentes[tipoJuego.ordinal() + 1].iterator();
                while ( it.hasNext() && (total < numAgentes) ) {
                    jugador = (AID) it.next();
                    if ( !listaJugadores.contains(jugador) && 
                         !agentesContactados.contains(jugador) ) {
                        msg.addReceiver(jugador);
                        agentesContactados.add(jugador);
                        total++;
                    }
                }
                
                Action ac = new Action(myAgent.getAID(), proponerJuego);

                try {
                    manager[tipoJuego.ordinal()].fillContent(msg, ac);
                } catch (Codec.CodecException | OntologyException ex) {
                    guiConsola.mensaje("Error en la construcción del mensaje en Proponer Juego \n" + ex);
                }
            }
            
            // Reiniciamos la tarea hasta que consigamos la cantidad de jugadores necesarios
            // si hay nuevos jugadores
            if ( msg.getAllReceiver().hasNext() ) {
                reset(msg);
            } else {
                guiConsola.mensaje("No se han encontrado suficientes jugadores para el juego: " + juego);
                registroJuegos.remove(juego.getIdJuego());
                numJuego++;
            }
        }
    }

    class TareaCompletarJuego extends ProposeInitiator {
        private final Juego juego;
        private final List agentesContactados;
        private boolean nuevoAgente;

        public TareaCompletarJuego(Agent a, ACLMessage msg, Juego juego) {
            super(a, msg);
            this.juego = juego;
            agentesContactados = new ArrayList();

            // Almacenamos con el primero que contactamos inicialmente 
            agentesContactados.add(msg.getAllReceiver().next());

            // Quedan agentes para intentarlo
            nuevoAgente = true;
        }

        @Override
        protected void handleOutOfSequence(ACLMessage msg) {
            // Ha llegado un mensaje fuera de la secuencia del protocolo
            guiConsola.mensaje("ERROR en Completar Juego_____________________\n" + msg);
        }

        @Override
        protected void handleAllResponses(Vector responses) {

            Iterator it = responses.iterator();
            TipoJuego tipoJuego = juego.getTipoJuego();
            while (it.hasNext()) {
                ACLMessage msg = (ACLMessage) it.next();
                switch ( msg.getPerformative() ) {
                    case ACLMessage.ACCEPT_PROPOSAL:
                        try {
                            JuegoAceptado juegoAceptado = (JuegoAceptado) manager[tipoJuego.ordinal()].extractContent(msg);
                            Grupo agenteGrupo = (Grupo) juegoAceptado.getAgenteJuego();
                            addRegistroGrupoJuegos(agenteGrupo, juego);
                            guiConsola.mensaje(juegoAceptado.toString());
                            juegosPendientes();
                        } catch (Codec.CodecException | OntologyException ex) {
                            guiConsola.mensaje("Error en la construcción del mensaje de " + msg.getSender().getLocalName() +
                                    "\n" + msg);
                        } catch ( Exception ex ) {
                            guiConsola.mensaje("Error inesperado de " + msg.getSender().getLocalName() + "\n" + ex);
                        } break;
                    case ACLMessage.REJECT_PROPOSAL:
                        try {
                            Motivacion motivacion = (Motivacion) manager[tipoJuego.ordinal()].extractContent(msg);
                            guiConsola.mensaje(motivacion.toString());
                            completarPropuesta();
                        } catch (Codec.CodecException | OntologyException ex) {
                            guiConsola.mensaje("Error en la construcción del mensaje de " + msg.getSender().getLocalName() +
                                    "\n" + msg);
                        } catch ( Exception ex ) {
                            guiConsola.mensaje("Error inesperado de " + msg.getSender().getLocalName() + "\n" + ex);
                        } break;
                    case ACLMessage.NOT_UNDERSTOOD:
                        // Juego no implementado en el agente Grupo Juegos
                        completarPropuesta();
                        if (!nuevoAgente) {
                            guiConsola.mensaje("No hay agentes para atender la propuesta de juego");
                        }   break;
                    default:
                        break;
                }
            }
        }

        /**
         * Contacta con el siguiente agente conocido para completar el juego
         */
        private void completarPropuesta() {
            Iterator it = listaAgentes[GRUPO_JUEGOS.ordinal()].iterator();
            AID agente = null;
            nuevoAgente = false;
            // Comprobamos que quedan agentes con los que no hemos contactado
            while ( it.hasNext() && !nuevoAgente ) {
                agente = (AID) it.next();
                if ( !agentesContactados.contains(agente) ) {
                    agentesContactados.add(agente);
                    nuevoAgente = true;
                }
            }
            
            if ( nuevoAgente ) {
                RegistroJuego juegoRegistrado = registroJuegos.get(juego.getIdJuego());
                TipoJuego tipoJuego = juego.getTipoJuego();
                Concept condicionesJuego = juegoRegistrado.getJuegoPropuesto().getTipoJuego();
                List listaJugadores = juegoRegistrado.getListaJugadores();
                CompletarJuego completarJuego = new CompletarJuego(juego, condicionesJuego, 
                                                         new jade.util.leap.ArrayList((ArrayList) listaJugadores));
            
                // Creamos el mensaje
                ACLMessage msg = new ACLMessage(ACLMessage.PROPOSE);
                msg.setProtocol(FIPANames.InteractionProtocol.FIPA_PROPOSE);
                msg.setSender(myAgent.getAID());
                msg.setLanguage(codec.getName());
                msg.setOntology(listaOntologias[tipoJuego.ordinal()].getName());
                msg.addReceiver(agente);
                msg.setReplyByDate(new Date(System.currentTimeMillis() + TIME_OUT));

                Action ac = new Action(myAgent.getAID(), completarJuego);
            
                try {
                    manager[tipoJuego.ordinal()].fillContent(msg, ac);
                } catch (Codec.CodecException | OntologyException ex) {
                    guiConsola.mensaje("Error en la construcción del mensaje en Completar Juego \n" + ex);
                }
                
                reset(msg);
            }
        }
    }
    
    class TareaInformarJuego extends SubscriptionInitiator {
        
        public TareaInformarJuego(Agent a, ACLMessage msg) {
            super(a, msg);
        }

        @Override
        protected void handleOutOfSequence(ACLMessage msg) {
            // Ha llegado un mensaje fuera de la secuencia del protocolo
            guiConsola.mensaje("ERROR en Informar Juego___________________\n" + msg);
        }

        /**
         * Llega la clasificación de los juegos del agente central juego
         * @param inform 
         */
        @Override
        protected void handleInform(ACLMessage inform) {
            // Versión preliminar
            DetalleInforme elemento;
            
            // Buscamos la ontología del mensaje
            int indiceJuego = buscarIndiceJuego(inform.getOntology());
            
            try {
                elemento = (DetalleInforme) manager[indiceJuego].extractContent(inform);
                
                if ( elemento.getDetalle() instanceof ClasificacionJuego ) {
                    // Presentamos la clasificación del juego
                    ClasificacionJuegoJFrame guiClasificacion;
                    ClasificacionJuego clasificacion = (ClasificacionJuego) elemento.getDetalle();
                    guiClasificacion = new ClasificacionJuegoJFrame(clasificacion);
                    guiConsola.mensaje("Fin Juego \n" + clasificacion.getJuego() + "\n" +
                            clasificacion.getListaJugadores() + "\n" + clasificacion.getListaPuntuacion());
                } else {
                    // Ha ocurrido un problema en el juego
                    Informe informe = (Informe) elemento.getDetalle();
                    guiConsola.mensaje("Error en el juego " + informe.getJuego() + "\n" 
                                       + " por " + informe.getDetalle());
                }
            } catch (Codec.CodecException | OntologyException ex) {
                guiConsola.mensaje("Error en el formato del mensaje del agente " + 
                                inform.getSender().getLocalName());
            } catch ( Exception ex ) {
               guiConsola.mensaje("Error inesperado de\n" + ex);
               Logger.getLogger(AgenteCentralJuego.class.getName()).log(Level.SEVERE, null, ex);         
            }
        }

        /**
         * El agente grupo de juegos rechaza la suscripción
         * @param refuse 
         */
        @Override
        protected void handleRefuse(ACLMessage refuse) {
            // Versión preliminar
            guiConsola.mensaje("El agente: " + refuse.getSender().getName() + " rechaza la suscripción");
            
        }

        /**
         * El agente grupo juegos acepta la suscripción
         * @param agree 
         */
        @Override
        protected void handleAgree(ACLMessage agree) {
            // Versión preliminar
            guiConsola.mensaje("El agente: " + agree.getSender().getName() + " acepta la suscripción");
        }
    }
}
