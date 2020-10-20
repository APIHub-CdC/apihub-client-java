package com.cdc.apihub.mx.TOE.test.RCC;

import com.cdc.apihub.mx.RCC.client.ApiClient;
import com.cdc.apihub.mx.RCC.client.ApiException;
import com.cdc.apihub.mx.RCC.client.ApiResponse;
import com.cdc.apihub.mx.RCC.client.api.RCCApi;
import com.cdc.apihub.mx.RCC.client.model.CatalogoEstados;
import com.cdc.apihub.mx.RCC.client.model.CatalogoTipoAsentamiento;
import com.cdc.apihub.mx.RCC.client.model.CatalogoTipoDomicilio;
import com.cdc.apihub.mx.RCC.client.model.Consultas;
import com.cdc.apihub.mx.RCC.client.model.Creditos;
import com.cdc.apihub.mx.RCC.client.model.DomicilioPeticion;
import com.cdc.apihub.mx.RCC.client.model.DomiciliosRespuesta;
import com.cdc.apihub.mx.RCC.client.model.Empleos;
import com.cdc.apihub.mx.RCC.client.model.PersonaPeticion;
import com.cdc.apihub.mx.RCC.client.model.Respuesta;

import com.cdc.apihub.signer.manager.interceptor.SignerInterceptor;

import okhttp3.OkHttpClient;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class ApiTest {
	
    private final RCCApi api = new RCCApi();

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
    public void getReporte_RCC_Test() throws ApiException {

		Boolean xFullReport = false;
		Integer estatusOK = 200;
		Integer estatusNoContent = 204;

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
		
		domicilio.setDireccion("HIDALGO 32");
		domicilio.setColoniaPoblacion("CENTRO");
		domicilio.setDelegacionMunicipio("LA BARCA");
		domicilio.setCiudad("BENITO JUAREZ");
		domicilio.setEstado(CatalogoEstados.JAL);
		domicilio.setCP("47917");
		domicilio.setFechaResidencia(null);
        domicilio.setTipoAsentamiento(CatalogoTipoAsentamiento._28);
        domicilio.setTipoDomicilio(CatalogoTipoDomicilio.C);
		
		persona.setDomicilio(domicilio);
        
        try {
			
			ApiResponse<?> response = api.getGenericReporte(xApiKey, usernameCDC, passwordCDC, persona, xFullReport.toString());
			
			Assert.assertTrue(estatusOK.equals(response.getStatusCode()));
			
			if (estatusOK.equals(response.getStatusCode())) {

				Respuesta responseOK = (Respuesta) response.getData();
				logger.info(responseOK.toString());
				if (responseOK.getFolioConsulta() != null && xFullReport ) {

					String folioConsulta = responseOK.getFolioConsulta();

					Consultas consultas = api.getConsultas(folioConsulta, xApiKey, usernameCDC, passwordCDC);
					Assert.assertTrue(consultas.getConsultas() != null);

					Creditos creditos = api.getCreditos(folioConsulta, xApiKey, usernameCDC, passwordCDC);
					Assert.assertTrue(creditos.getCreditos() != null);

					DomiciliosRespuesta domicilios = api.getDomicilios(folioConsulta, xApiKey, usernameCDC, passwordCDC);
					Assert.assertTrue(domicilios.getDomicilios() != null);

					Empleos empleos = api.getEmpleos(folioConsulta, xApiKey, usernameCDC, passwordCDC);
					Assert.assertTrue(empleos.getEmpleos() != null);
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