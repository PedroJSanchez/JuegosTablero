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
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.proto.ProposeInitiator;
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
import static juegosTablero.Vocabulario.NombreServicio.GRUPO_JUEGOS;
import static juegosTablero.Vocabulario.TipoJuego.DOMINO;
import juegosTablero.aplicacion.barcos.JuegoBarcos;
import juegosTablero.aplicacion.conecta4.JuegoConecta4;
import juegosTablero.aplicacion.domino.JuegoDomino;
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
import juegosTablero.dominio.elementos.Grupo;
import smma.juegosTablero.gui.AgentesJuegoJFrame;
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
            Logger.getLogger(AgenteCentralJuego.class.getName()).log(Level.SEVERE, null, ex);
            this.doDelete();
        }

        //Añadir tareas principales
        addBehaviour(new TareaBuscarAgentes(this, BUSCAR_AGENTES));

    }

    @Override
    protected void takeDown() {
        // Liberación de los recursos del agente
        guiConsola.dispose();
        guiAgente.dispose();
        guiAgentesJuego.dispose();

        System.out.println("Finaliza la ejecución de " + this.getName());
    }

    /**
     * Seleccionamos uno de los juegos disponibles aleatoriamente
     *
     * @return un tipo de juego
     */
    public TipoJuego buscarJuego() {

        int indiceJuego = aleatorio.nextInt(tipoJuegosActivos.size());

        return tipoJuegosActivos.get(indiceJuego);
    }

    private void addAgentesJugador(ACLMessage msg, TipoJuego tipoJuego, ModoJuego modo, int numAgentes) {
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
            addSuscripcion(agenteGrupo);
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
    
    public void proponerJuego(TipoJuego tipoJuego) {
        ProponerJuego proponerJuego = new ProponerJuego();
        ModoJuego modoJuego = ModoJuego.values()[aleatorio.nextInt(ModoJuego.values().length)];
        Juego juego = new Juego(tipoJuego.name() + "_" + numJuego, MEJOR_UNO.victorias(), modoJuego, tipoJuego);
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

        ACLMessage msg = new ACLMessage(ACLMessage.PROPOSE);
        msg.setProtocol(FIPANames.InteractionProtocol.FIPA_PROPOSE);
        msg.setSender(getAID());
        msg.setLanguage(codec.getName());
        msg.setOntology(listaOntologias[tipoJuego.ordinal()].getName());
        addAgentesJugador(msg, tipoJuego, modoJuego, minJugadores[tipoJuego.ordinal()]);
        msg.setReplyByDate(new Date(System.currentTimeMillis() + TIME_OUT));

        Action ac = new Action(this.getAID(), proponerJuego);
        
        try {
            manager[tipoJuego.ordinal()].fillContent(msg, ac);
        } catch (Codec.CodecException | OntologyException ex) {
            Logger.getLogger(AgenteCentralJuego.class.getName()).log(Level.SEVERE, null, ex);
        }

        // Registramos el juego
        addRegistroJuego(proponerJuego);

        addBehaviour(new TareaProponerJuego(this, msg, juego, minJugadores[tipoJuego.ordinal()]));

        guiConsola.mensaje(msg.toString());
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
                    Logger.getLogger(AgenteCentralJuego.class.getName()).log(Level.SEVERE, null, ex);
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
    private void addSuscripcion(Grupo agenteGrupo) {

    }

    /**
     * Tarea que localizará los agentes grupo juegos y agentes jugadores
     * presentes en la plataforma para el juego.
     */
    class TareaBuscarAgentes extends TickerBehaviour {

        //Se buscarán agentes consola y operación
        public TareaBuscarAgentes(Agent a, long period) {
            super(a, period);
        }

        @Override
        protected void onTick() {
            DFAgentDescription template;
            ServiceDescription sd;
            DFAgentDescription[] resultado;

            tipoJuegosActivos.clear();

            //Buscar los agentes implicados en los juegos
            template = new DFAgentDescription();
            sd = new ServiceDescription();
            for (NombreServicio servicio : tiposAgentes) {
                // Limpiamos la lista
                listaAgentes[servicio.ordinal()].clear();
                sd.setName(servicio.name());
                template.addServices(sd);
                try {
                    resultado = DFService.search(myAgent, template);
                    if (resultado.length > NO_HAY_AGENTES) {
                        for (int i = 0; i < resultado.length; ++i) {
                            listaAgentes[servicio.ordinal()].add(resultado[i].getName());
                        }
                    }
                    if (!servicio.equals(GRUPO_JUEGOS) && (resultado.length >= minJugadores[servicio.ordinal() - 1])) {
                        // Juegos que tienen el mínimo de jugadores necesarios
                        tipoJuegosActivos.add(listaJuegos[servicio.ordinal() - 1]);
                        guiAgente.activaProponerJuego();
                    } else if ( servicio.equals(GRUPO_JUEGOS) ) {
                        juegosPendientes();
                    }

                    guiAgentesJuego.addAgente(listaAgentes[servicio.ordinal()], servicio);
                } catch (FIPAException ex) {
                    Logger.getLogger(AgenteCentralJuego.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    class TareaProponerJuego extends ProposeInitiator {

        private final Juego juego;
        private final int numJugadores;

        public TareaProponerJuego(Agent a, ACLMessage msg, Juego juego, int numJugadores) {
            super(a, msg);
            this.juego = juego;
            this.numJugadores = numJugadores;
        }

        @Override
        protected void handleOutOfSequence(ACLMessage msg) {
            // Ha llegado un mensaje fuera de la secuencia del protocolo
            guiConsola.mensaje("ERROR________________________\n" + msg);
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
                if (msg.getPerformative() == ACLMessage.ACCEPT_PROPOSAL) {
                    try {
                        // Acepta la propuesta
                        JuegoAceptado juegoAceptado = (JuegoAceptado) manager[tipoJuego.ordinal()].extractContent(msg);
                        Jugador jugador = (Jugador) juegoAceptado.getAgenteJuego();
                        addRegistroJugador(juego, jugador);
                        guiConsola.mensaje("El agente: " + jugador.getNombre() + " acepta el juego " + juego);

                    } catch (Codec.CodecException | OntologyException ex) {
                        guiConsola.mensaje("Error: " + ex);
                    }
                } else {
                    try {
                        // Rechaza la propuesta
                        Motivacion motivacion = (Motivacion) manager[tipoJuego.ordinal()].extractContent(msg);
                        guiConsola.mensaje("El agente: " + msg.getSender().getLocalName()
                                + " rechaza el juego: " + juego + " por: " + motivacion);
                    } catch (Codec.CodecException | OntologyException ex) {
                        guiConsola.mensaje("Error: " + ex);
                    }
                }
            }

            RegistroJuego juegoRegistrado = registroJuegos.get(idJuego);
            if (juegoRegistrado.numJugadores() < numJugadores) {
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
            if (modoJuego.equals(TORNEO)) {
                Iterator it = listaAgentes[tipoJuego.ordinal() + 1].iterator();
                while (it.hasNext()) {
                    jugador = (AID) it.next();
                    if (!listaJugadores.contains(jugador)) {
                        msg.addReceiver(jugador);
                    }
                }
            } else {
                // Jugadores que nos faltan
                int numAgentes = numJugadores - listaJugadores.size();
                Iterator it = listaAgentes[tipoJuego.ordinal() + 1].iterator();
                int total = 0;
                while (it.hasNext() && (total < numAgentes)) {
                    jugador = (AID) it.next();
                    if (!listaJugadores.contains(jugador)) {
                        msg.addReceiver(jugador);
                        total++;
                    }
                }
            }

            Action ac = new Action(myAgent.getAID(), proponerJuego);

            try {
                manager[tipoJuego.ordinal()].fillContent(msg, ac);
            } catch (Codec.CodecException | OntologyException ex) {
                Logger.getLogger(AgenteCentralJuego.class.getName()).log(Level.SEVERE, null, ex);
            }

            // Reiniciamos la tarea hasta que consigamos la cantidad de jugadores necesarios
            // si tenemos alternativas
            if (msg.getAllReceiver().hasNext()) {
                reset(msg);
            } else {
                guiConsola.mensaje("No se han encontrado suficientes jugadores para el juego: " + juego);
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
            guiConsola.mensaje("ERROR________________________\n" + msg);
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
                            Logger.getLogger(AgenteCentralJuego.class.getName()).log(Level.SEVERE, null, ex);
                        }   break;
                    case ACLMessage.REJECT_PROPOSAL:
                        try {
                            Motivacion motivacion = (Motivacion) manager[tipoJuego.ordinal()].extractContent(msg);
                            guiConsola.mensaje(motivacion.toString());
                            completarPropuesta();
                        } catch (Codec.CodecException | OntologyException ex) {
                            Logger.getLogger(AgenteCentralJuego.class.getName()).log(Level.SEVERE, null, ex);
                        }   break;
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
                    Logger.getLogger(AgenteCentralJuego.class.getName()).log(Level.SEVERE, null, ex);
                }
                
                reset(msg);
            }
        }
    }
}
