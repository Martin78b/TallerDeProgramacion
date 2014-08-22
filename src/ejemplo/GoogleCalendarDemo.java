/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ejemplo;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.java6.auth.oauth2.FileCredentialStore;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Calendar;
import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.Events;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collections;
import java.util.Date;
import java.util.TimeZone;

/**
 * Clase principal de la demostración de uso de la API de Google Calendar.
 * @author Esteban
 */
public class GoogleCalendarDemo {

    /**
     * Instancia global de HttpTransport.
     */
    private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
    /**
     * Instancia global de la fábrica de objetos JSON.
     */
    private static final JsonFactory JSON_FACTORY = new JacksonFactory();
    
    /**
     * Instancia del servicio de calendario.
     */
    private static com.google.api.services.calendar.Calendar cCalendarService;

    /**
     * Autoriza a la aplicación a acceder a los datos protegidos del servicio de
     * Google Calendar.
     *
     * @return Credencial de la aplicación.
     * @throws Exception Ante la ocurrencia de error durante la autorización.
     */
    private static Credential authorize() throws Exception {
        // Se leen los datos de acceso al servicio de calendario, a partir del
        // archivo client_secrets.json, el cual se encuentra en formato JSON
        // (JavaScript Object Notation). Para ello se usa la fábrica de objetos
        // JSON y el archivo de configuración se carga como un recurso de l
        // aplicación.
        GoogleClientSecrets mClientSecrets;
        Reader lector = new InputStreamReader(GoogleCalendarDemo.class.getResourceAsStream("./client_secrets.json"));
        mClientSecrets = GoogleClientSecrets.load(JSON_FACTORY, lector);
       
        // Se configura el repositorio que se utilizará para almacenar las credenciales.
        FileCredentialStore mCredentialStore =
                new FileCredentialStore(new File(System.getProperty("user.home"), ".credentials/calendar.json"), JSON_FACTORY);

        // Se configura el flujo de autorización de la aplicación.
        GoogleAuthorizationCodeFlow mAuthorizationFlow =
                new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT,
                                                        JSON_FACTORY,
                                                        mClientSecrets,
                                                        Collections.singleton(CalendarScopes.CALENDAR))
                .setCredentialStore(mCredentialStore)
                .build();

        // Finalmente, se intenta autorizar la aplicación contra el servicio de Google Calendar.
        return new AuthorizationCodeInstalledApp(mAuthorizationFlow,
                                                 new LocalServerReceiver()).authorize("user");
    }

    /**
     * Punto de entrada de la aplicación.
     * @param pArgs Argumentos de la aplicación.
     */
    public static void main(String[] pArgs) {
        try {
            try {
                // Se intenta autorizar a la aplicación para acceder al
                // servicio de Google Calendar.
                Credential mCredential = authorize();

                // Como se logró la autorización para el acceso al servicio de
                // calendario, el mismo es configurado.
                cCalendarService = new com.google.api.services.calendar.Calendar
                        .Builder(HTTP_TRANSPORT,
                                 JSON_FACTORY,
                                 mCredential).setApplicationName("Google-CalendarSample/1.0")
                        .build();
                
                // Visualización de calendarios del usuario.
                showCalendars();
                
                System.out.println("");
                
                // Se agrega un calendario.
                Calendar mCalendar = addCalendar();
                
                System.out.println("");
                
                // Actualización de calendario.
                updateCalendar(mCalendar);
                
                System.out.println("");
                
                // Se agrega un evento al calendario.
                addEvent(mCalendar);
                
                System.out.println("");
                
                // Se muestran los eventos del calendario.
                showEvents(mCalendar);
                
                System.out.println("");
                
                // Se elimina el calendario. Descomentar para probar.
                deleteCalendar(mCalendar);

            } catch (IOException bEx) {
                System.err.println(bEx.getMessage());
            }
        } catch (Throwable bEx) {
            bEx.printStackTrace();
        }
    }

    /**
     * Muestra los calendarios del usuario.
     * @throws IOException Ante la ocurrencia de un error de I/O.
     */
    private static void showCalendars() throws IOException {
        System.out.println("Listado de calendarios");
        System.out.println("===================================================");
        
        // Se obtiene los calendarios del usuario.
        CalendarList mCalendars = cCalendarService.calendarList().list().execute();
        
        if (mCalendars.getItems() == null) {
            System.out.println("El usuario no posee calendarios.");
        } else {
            // Si el usuario posee al menos un calendario, entonces se imprime
            // por pantalla el identificador y el resumen del mismo, y la
            // descripción en caso de que la tenga.
            for (CalendarListEntry bCalendar : mCalendars.getItems()) {
                
                System.out.println("Identificador: " + bCalendar.getId());
                
                System.out.println("Resumen: " + bCalendar.getSummary());
                
                if (bCalendar.getDescription() != null) {
                  System.out.println("Descripción: " + bCalendar.getDescription());
                }
            }
        }
    }

    /**
     * Agrega un calendario.
     * @return Calendario agregado.
     * @throws IOException Ante la ocurrencia de un error de I/O.
     */
    private static Calendar addCalendar() throws IOException {
        System.out.println("Creación de calendario");
        System.out.println("===================================================");
        
        Calendar mCalendar = new Calendar();
        mCalendar.setSummary("Taller de Programación");
        mCalendar.setDescription("3ero ISI");
        mCalendar.setLocation("UTN - FRCU - Concepción del Uruguay - ER");
        
        // El resultado debe asignarse a una variable (la misma que se utilizó
        // originalmente, como este caso u otra diferente) porque hay algunos
        // datos, como por ejemplo el identificador del calendario, que son
        // asignados por el servicio.
        mCalendar = cCalendarService.calendars().insert(mCalendar).execute();
        
        displayCalendar(mCalendar);
        
        return mCalendar;
    }

    /**
     * Actualiza los datos del calendario proporcionado como parámetro.
     * @param pCalendar Calendario a actualizar.
     * @return Calendario actualizado.
     * @throws IOException Ante la ocurrencia de un error de I/O.
     */
    private static Calendar updateCalendar(Calendar pCalendar) throws IOException {
        System.out.println("Actualización de calendario");
        System.out.println("===================================================");
        
        Calendar mCalendar = new Calendar();
        mCalendar.setSummary("Taller de Programación - Resumen del calendario actualizado.");
        
        mCalendar = cCalendarService.calendars().patch(pCalendar.getId(), mCalendar).execute();
        
        displayCalendar(mCalendar);
        
        return mCalendar;
    }

    /**
     * Agrega un evento al calendario.
     * @param pCalendar Calendario sobre el que se agrega el evento.
     * @throws IOException Ante la ocurrencia de un error de I/O.
     */
    private static void addEvent(Calendar pCalendar) throws IOException {
        System.out.println("Creación de evento sobre el calendario con id." + pCalendar.getId());
        System.out.println("===================================================");
        
        Event mEvent = new Event();
        mEvent.setSummary("Entrega de TP de BBDD");
        Date mStartDate = new Date();
        Date mEndDate = new Date(mStartDate.getTime() + 3600000); // Una hora más que la inicial.
        
        // Notar como se utiliza otra clase que no es la Date de Java.
        DateTime mStart = new DateTime(mStartDate, TimeZone.getTimeZone("UTC"));
        mEvent.setStart(new EventDateTime().setDateTime(mStart));
        DateTime mEnd = new DateTime(mEndDate, TimeZone.getTimeZone("UTC"));
        mEvent.setEnd(new EventDateTime().setDateTime(mEnd));
        
        mEvent = cCalendarService.events().insert(pCalendar.getId(), mEvent).execute();
        
        displayEvent(mEvent);
    }

    /**
     * Imprime por pantalla los eventos del calendario suministrado como parámetro.
     * @param pCalendar Calendario cuyos eventos se visualizarán.
     * @throws IOException Ante la ocurrencia de un error de I/O.
     */
    private static void showEvents(Calendar pCalendar) throws IOException {
        System.out.println("Listado de eventos");
        System.out.println("===================================================");
        Events mEvents = cCalendarService.events().list(pCalendar.getId()).execute();
        
        if (mEvents.getItems() == null) {
            System.out.println("No existen eventos en el calendario con id. " + pCalendar.getId());
        } else {
            for (Event bEvent : mEvents.getItems()) {
                displayEvent(bEvent);
            }
        }
    }

    /**
     * Elimina el calendario proporcionado como parámetro.
     * @param pCalendar Calendario a eliminar.
     * @throws IOException Ante la ocurrencia de un error de I/O.
     */
    private static void deleteCalendar(Calendar pCalendar) throws IOException {
        System.out.println("Eliminación de calendario con id. " + pCalendar.getId());
        System.out.println("===================================================");
        cCalendarService.calendars().delete(pCalendar.getId()).execute();
    }

    /**
     * Imprime por pantalla los datos de un calendario.
     * @param pCalendar Calendario cuyos datos desean imprimirse por pantalla.
     */
    private static void displayCalendar(Calendar pCalendar) {
        System.out.println("Identificador: " + pCalendar.getId());
                
        System.out.println("Resumen: " + pCalendar.getSummary());

        if (pCalendar.getDescription() != null) {
            System.out.println("Descripción: " + pCalendar.getDescription());
        }
        
        if (pCalendar.getLocation() != null) {
            System.out.println("Ubicación: " + pCalendar.getLocation());
        }
    }
    
    /**
     * Imprime por pantalla los datos de un evento.
     * @param pEvent Evento cuyos datos se desean imprimir.
     */
    private static void displayEvent(Event pEvent) {
        System.out.println("Identificador: " + pEvent.getId());
        
        if (pEvent.getDescription() != null) {
            System.out.println("Descripción: " + pEvent.getDescription());
        }
        
        if (pEvent.getStart() != null) {
            System.out.println("Inicio: " + pEvent.getStart());
        }
        
        if (pEvent.getEnd() != null) {
            System.out.println("Fin: " + pEvent.getEnd());
        }
    }
    
}
