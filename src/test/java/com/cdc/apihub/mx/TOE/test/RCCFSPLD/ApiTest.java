package com.cdc.apihub.mx.TOE.test.RCCFSPLD;

import okhttp3.OkHttpClient;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cdc.apihub.signer.manager.interceptor.SignerInterceptor;

import com.cdc.apihub.mx.RCC.FS.PLD.client.ApiClient;
import com.cdc.apihub.mx.RCC.FS.PLD.client.ApiException;
import com.cdc.apihub.mx.RCC.FS.PLD.client.ApiResponse;
import com.cdc.apihub.mx.RCC.FS.PLD.client.api.RCCFSPLDApi;
import com.cdc.apihub.mx.RCC.FS.PLD.client.model.CatalogoEstados;
import com.cdc.apihub.mx.RCC.FS.PLD.client.model.Consultas;
import com.cdc.apihub.mx.RCC.FS.PLD.client.model.Creditos;
import com.cdc.apihub.mx.RCC.FS.PLD.client.model.DomicilioPeticion;
import com.cdc.apihub.mx.RCC.FS.PLD.client.model.DomiciliosRespuesta;
import com.cdc.apihub.mx.RCC.FS.PLD.client.model.Empleos;
import com.cdc.apihub.mx.RCC.FS.PLD.client.model.Mensajes;
import com.cdc.apihub.mx.RCC.FS.PLD.client.model.PersonaPeticion;
import com.cdc.apihub.mx.RCC.FS.PLD.client.model.Respuesta;
import com.cdc.apihub.mx.RCC.FS.PLD.client.model.Scores;

import org.junit.Before;
import org.junit.Assert;

import java.util.concurrent.TimeUnit;

public class ApiTest {
	private final RCCFSPLDApi api = new RCCFSPLDApi();
	private Logger logger = LoggerFactory.getLogger(ApiTest.class.getName());

	private ApiClient apiClient;

	private String keystoreFile = "your_path_for_your_keystore/keystore.jks";
	private String cdcCertFile = "your_path_for_certificate_of_cdc/cdc_cert.pem";
	private String keystorePassword = "your_super_secure_keystore_password";
	private String keyAlias = "your_key_alias";
	private String keyPassword = "your_super_secure_password";
	
	private String usernameCDC = "your_username_otrorgante";
	private String passwordCDC = "your_password_otorgante";	
	
	private String url = "the_url";
	private String xApiKey = "X_Api_Key";
	
	private Boolean xFullReport = true;
	private Integer estatusOK = 200;
	private Integer estatusNoContent = 204;
	
	private SignerInterceptor interceptor;
	
	@Before()
	public void setUp() {
		interceptor = new SignerInterceptor(keystoreFile, cdcCertFile, keystorePassword, keyAlias, keyPassword);
		this.apiClient = api.getApiClient();
		this.apiClient.setBasePath(url);
		OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
			    .readTimeout(30, TimeUnit.SECONDS)
			    .addInterceptor(interceptor)
			    .build();
		apiClient.setHttpClient(okHttpClient);
	}

	@Test
	public void getReporte_RCCFSPLD_Test() throws ApiException {
		
		PersonaPeticion persona = new PersonaPeticion();
		DomicilioPeticion domicilio = new DomicilioPeticion();
		
		persona.setApellidoPaterno("PATERNO");
		persona.setApellidoMaterno("MATERNO");
		persona.setApellidoAdicional(null);
		persona.setPrimerNombre("PRIMERNOMBRE");
	    persona.setSegundoNombre(null);
	    persona.setFechaNacimiento("1952-05-13");
	    persona.setRFC("PAMP010101");
	    persona.setCURP(null);
	    persona.setNacionalidad("MX");
	    persona.setResidencia(null);
	    persona.setEstadoCivil(null);
	    persona.setSexo(null);
	    persona.setClaveElectorIFE(null);
	    persona.setNumeroDependientes(null);
	    persona.setFechaDefuncion(null);
	    persona.setDomicilio(null);
		
		domicilio.setDireccion("HIDALGO 32");
		domicilio.setColoniaPoblacion("CENTRO");
		domicilio.setDelegacionMunicipio("LA BARCA");
		domicilio.setCiudad("BENITO JUAREZ");
		domicilio.setEstado(CatalogoEstados.JAL);
		domicilio.setCP("47917");
		domicilio.setFechaResidencia(null);
		domicilio.setNumeroTelefono(null);
		domicilio.setTipoDomicilio(null);
		domicilio.setTipoAsentamiento(null);
		
		persona.setDomicilio(domicilio);
		
		try {
			
			ApiResponse<?> response = api.getGenericReporte(xApiKey, usernameCDC, passwordCDC, persona, xFullReport);
			
			Assert.assertTrue(estatusOK.equals(response.getStatusCode()));
			
			if (estatusOK.equals(response.getStatusCode())) {
				Respuesta responseOK = (Respuesta) response.getData();
				logger.info(responseOK.toString());
				
				if (responseOK.getFolioConsulta() != null && !xFullReport ) {
					String folioConsulta = responseOK.getFolioConsulta();

					Consultas consultas2 = api.getConsultas(folioConsulta, xApiKey, usernameCDC, passwordCDC);
					Assert.assertTrue(consultas2.getConsultas() != null);

					Creditos creditos = api.getCreditos(folioConsulta, xApiKey, usernameCDC, passwordCDC);
					Assert.assertTrue(creditos.getCreditos() != null);

					DomiciliosRespuesta domicilios = api.getDomicilios(folioConsulta, xApiKey, usernameCDC, passwordCDC);
					Assert.assertTrue(domicilios.getDomicilios() != null);

					Empleos empleos = api.getEmpleos(folioConsulta, xApiKey, usernameCDC, passwordCDC);
					Assert.assertTrue(empleos.getEmpleos() != null);

					Scores scores = api.getScores(folioConsulta, xApiKey, usernameCDC, passwordCDC);
					Assert.assertTrue(scores.getScores() != null);
					
					Mensajes mensajes = api.getMensajes(folioConsulta, xApiKey, usernameCDC, passwordCDC);
					Assert.assertTrue(mensajes.getMensajes() != null);
				}				
			}

		} catch (ApiException e) {
			
			if (!estatusNoContent.equals(e.getCode())) {
				logger.info("Response received from API: "+interceptor.getErrores().toString());
				logger.info("Response processed by client:"+ e.getResponseBody());
			} else {
				logger.info("The response was a status 204 (NO CONTENT)");
			}
			
			Assert.assertTrue(estatusOK.equals(e.getCode()));
		}	

	}
}
