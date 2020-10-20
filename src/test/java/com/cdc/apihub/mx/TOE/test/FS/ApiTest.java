package com.cdc.apihub.mx.TOE.test.FS;

import com.cdc.apihub.mx.FS.client.ApiException;
import com.cdc.apihub.mx.FS.client.ApiResponse;
import com.cdc.apihub.mx.FS.client.api.FSApi;
import com.cdc.apihub.mx.FS.client.ApiClient;
import com.cdc.apihub.mx.FS.client.model.*;
import com.cdc.apihub.signer.manager.interceptor.SignerInterceptor;

import okhttp3.OkHttpClient;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.Assert;
import org.junit.Before;

import java.util.concurrent.TimeUnit;

public class ApiTest {

	private Logger logger = LoggerFactory.getLogger(ApiTest.class.getName());

    private final FSApi api = new FSApi();
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
    public void getReporte_FS_Test() throws ApiException {


        Peticion peticion = new Peticion();
        Domicilio domicilio = new Domicilio();

		peticion.setFolio("00000001");

		Persona persona = new Persona();
		persona.setNombres("NOMBRES");
		persona.setApellidoPaterno("PATERNO");
		persona.setApellidoMaterno("MATERNO");
	    persona.setFechaNacimiento("1952-05-13");
	    persona.setRFC("PAMP010101");
	    persona.setNacionalidad("MX");		
		
		domicilio.setDireccion("HIDALGO 32");
		domicilio.setColoniaPoblacion("CENTRO");
		domicilio.setDelegacionMunicipio("LA BARCA");
		domicilio.setCiudad("BENITO JUAREZ");
		domicilio.setEstado(CatalogoEstados.JAL);
		domicilio.setCP("47917");
        domicilio.setTipoAsentamiento(CatalogoTipoAsentamiento._28);
        domicilio.setTipoDomicilio(CatalogoTipoDomicilio.C);

		persona.setDomicilio(domicilio);

		peticion.setPersona(persona);
        
		try {
	        
			ApiResponse<?> response = api.getGenericReporte(xApiKey, usernameCDC, passwordCDC, peticion);
			
			Assert.assertTrue(estatusOK.equals(response.getStatusCode()));
			
			if (estatusOK.equals(response.getStatusCode())) {

				Respuesta responseOK = (Respuesta) response.getData();
				logger.info(responseOK.toString());
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
