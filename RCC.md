# Reporte de Cédito Consolidado


## Dependencia

A continución se presenta la dependencia contenedora del proyecto::

```shell
<project>
	<groupId>com.cdc.apihub.mx.TOE</groupId>
	<artifactId>apihub-client-java</artifactId>
	...
	<dependencies>
		<!-- API HUB dependencies -->
		<dependency>
		<dependency>
			<groupId>com.cdc.apihub.mx.RCC</groupId>
			<artifactId>rcc-client-java</artifactId>
			<version>...</version>
		</dependency>
		<!-- -->
	</dependencies>
</project>

```

## Preparar la petición

En el archivo ApiTest.java, que se encuentra en ***src/test/java/com/cdc/apihub/mx/TOE/test/RCC/***. Se deberá modificar los datos de la petición y los datos de consumo:

1. Configurar ubicación y acceso de la llave y el certificado
   - keystoreFile: ubicacion del archivo keystore.jks
   - cdcCertFile: ubicacion del archivo cdc_cert.pem
   - keystorePassword: contraseña de cifrado del keystore
   - keyAlias: alias asignado al keystore
   - keyPassword: contraseña de cifrado del contenedor

2. Credenciales de acceso dadas por Círculo de Crédito, obtenidas despues de la afiliación
   - usernameCDC: usuario de Círculo de Crédito
   - passwordCDC: contraseña de Círculo de Crédito
	
2. Datos de consumo del API
   - url: URL de la exposicón del API
   - xApiKey: Llave de identificación de la aplicación creada en el portal y nombrada como Consumer Key.

> **NOTA:** Los datos de la siguiente petición son solo representativos.

```java

package com.cdc.apihub.mx.TOE.test.RCC;

...

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
```