package com.example.rest.service;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.awt.image.BufferedImage;

import java.io.*;
import java.util.Base64;

import javax.imageio.ImageIO;
import SecuGen.FDxSDKPro.jni.*; 
import SecuGen.FDxSDKPro.jni.JSGFPLib; 

@RestController
public class SecugenController {

	public static long check ;
	public static long err ;
	public static long minex ;
	public static long porta ;
	public static long leitora ;
	public static long imagemGerada ;
	public static boolean imagemAux = false; 
    byte[] imageBuffer1;
    byte[] imageBuffer2;
    byte[] SG400minutiaeBuffer1;
    byte[] ANSIminutiaeBuffer1;
    byte[] ISOminutiaeBuffer1;
    byte[] SG400minutiaeBuffer2;
    byte[] ANSIminutiaeBuffer2;
    byte[] ISOminutiaeBuffer2;
    FileOutputStream fout = null;
    PrintStream fp = null;
    String base64Image=null;
    String base64File=null;

    @GetMapping("/capturar")
    public Secugen fingerprint()
    {
    	 /**
         * Inicializa um temporizador para fazer a requisição esperar 30segundos para finalizar caso não ache nenhum dedo
         */
    	long startTime = System.currentTimeMillis();
    	long endTime = startTime + 15000;
    	
        JSGFPLib sgfplib = new JSGFPLib();
        if((sgfplib !=null) &&(sgfplib.jniLoadStatus!= SGFDxErrorCode.SGFDX_ERROR_JNI_DLLLOAD_FAILED))
        {
            System.out.println(sgfplib);
        }
        else{
            return new Secugen(false,"Dispositivo não encontrado!","Aplicação não irá funcionar!");
        }

       /**
        * Inicializa o dispositivo
        */
       leitora= sgfplib.Init(SGFDxDeviceName.SG_DEV_AUTO);
       
       
        /**
         * GETING MINEX VERSION ---- não sei pra que serve
         */
        int[] extractorVersion = new int[1];
        int[] matcherVersion = new int[1];
        /**
         * CALL MINEX VERSION GetMinexVersion()
         */
        minex = sgfplib.GetMinexVersion(extractorVersion, matcherVersion);
        System.out.println(err);

       /**
        * Ligando a leitora biometrica , OpenDevice(number PORT)
        * Pega a porta usb automaticamente , OpenDevice(SGPPPortAddr.AUTO_DETECT)
        */
        
	    porta = sgfplib.OpenDevice(SGPPPortAddr.USB_AUTO_DETECT);

    
        /**
         * pega as informações do dispositvo
         */
       SGDeviceInfoParam deviceInfo = new SGDeviceInfoParam();
       leitora = sgfplib.GetDeviceInfo(deviceInfo);
  
       System.out.println("\tdeviceInfo.DeviceSN:    [" + new String(deviceInfo.deviceSN()) + "]");
       System.out.println("\tdeviceInfo.Brightness:  [" + deviceInfo.brightness + "]");
       System.out.println("\tdeviceInfo.ComPort:     [" + deviceInfo.comPort + "]");
       System.out.println("\tdeviceInfo.ComSpeed:    [" + deviceInfo.comSpeed + "]");
       System.out.println("\tdeviceInfo.Contrast:    [" + deviceInfo.contrast + "]");
       System.out.println("\tdeviceInfo.DeviceID:    [" + deviceInfo.deviceID + "]");
       System.out.println("\tdeviceInfo.FWVersion:   [" + deviceInfo.FWVersion + "]");
       System.out.println("\tdeviceInfo.Gain:        [" + deviceInfo.gain + "]");
       System.out.println("\tdeviceInfo.ImageDPI:    [" + deviceInfo.imageDPI + "]");
       System.out.println("\tdeviceInfo.ImageHeight: [" + deviceInfo.imageHeight + "]");
       System.out.println("\tdeviceInfo.ImageWidth:  [" + deviceInfo.imageWidth + "]"); 

        /**
         * TURNING LED ON/OFF
         * TURN ON : sgfplib.SetLedOn(true)
         * TURN OFF : sgfplib.SetLedOn(false)
         * err = sgfplib.SetLedOn(true);
         */

    int[] quality = new int[1];
    int[] maxSize = new int[1];
    int[] size = new int[1];
    SGFingerInfo fingerInfo = new SGFingerInfo();
    fingerInfo.FingerNumber = SGFingerPosition.SG_FINGPOS_LI;
    fingerInfo.ImageQuality = quality[0];
    fingerInfo.ImpressionType = SGImpressionType.SG_IMPTYPE_LP;
    fingerInfo.ViewNumber = 1;

    /**
     * captura a imagem ad digital
     */
    leitora =sgfplib.SetLedOn(true);  
    imageBuffer1 = new byte[deviceInfo.imageHeight*deviceInfo.imageWidth];
    /**
     * Standby da leitora , só sai do looping quando encontrar um dedo e tirar a imagem dele
     */
    //LocalDate segundosAgr = ;
    //LocalDate segundiMaximaLoop = segundosAgr + TIMEOUT_BIOMETRIA_MILISEGUNDOS ;  
    do {
    	
    	try{
    		err = sgfplib.GetImage(imageBuffer1);
    		/**
    		 * SGFDX_ERROR_NONE é o sucesso para capturar a imagem
    		 */
    		//System.out.println(err);
    		
    		if (err == SGFDxErrorCode.SGFDX_ERROR_NONE)
    		{
    			/**
    			 * qualidade da imagem
    			 */
    			err = sgfplib.GetImageQuality(deviceInfo.imageWidth, deviceInfo.imageHeight, imageBuffer1, quality);
    			/**
    			 * System.out.println("GetImageQuality returned : [" + err + "]");
    			 * System.out.println("Image Quality is : [" + quality[0] + "]");
    			 */
    			
    			
    			
    			
    			byte[][] buffer2D = new byte[deviceInfo.imageHeight][deviceInfo.imageWidth];  
    			for(int i=0;i<deviceInfo.imageHeight;i++) {
    				for(int j=0;j<deviceInfo.imageWidth;j++) {
    					buffer2D[i][j]=imageBuffer1[i*deviceInfo.imageWidth+j];
    				}
    			}
    			
    			
    			/**
    			 * Cria o base64 da imagem
    			 */
    			BufferedImage image = new BufferedImage(buffer2D.length, buffer2D[0].length, BufferedImage.TYPE_BYTE_GRAY);
    			for (int x = 0; x < buffer2D.length; x++) {
    				for (int y = 0; y <buffer2D[0].length; y++) {
    					image.setRGB(x, y, buffer2D[x][y]);
    				}
    			}
    			
    			ByteArrayOutputStream bos = new ByteArrayOutputStream();
    			try{
    				ImageIO.write(image, "jpg", bos);
    				byte[] imageBytes = bos.toByteArray();
    				
    				base64Image = Base64.getEncoder().encodeToString(imageBytes);
    				
    				bos.close();
    			}catch (IOException e) {
    				e.printStackTrace();
    			}
    			imagemAux = true;
    			/**
    			 * Fim da criação do base64
    			 */
    			
    		}
    		else
    		{
    			/**
    			 * Erro ao criar o base64 
    			 */
    			
    		}
    	}
    	catch(Exception e)
    	{
    		/**
    		 * Desliga o led
    		 */
    		leitora =sgfplib.SetLedOn(false);
    		
    		
    	}
    } while(imagemAux == false && System.currentTimeMillis() < endTime  ) ;
    //&& segundosAgr <= segundiMaximaLoop
    System.out.println("imagem " +imagemAux);
    imagemAux = false;

    /**
    * Desliga o led
     */
    leitora =sgfplib.SetLedOn(false);


    /**
     * SET TEMPLATE FORMAT ISO19794
     */
    imagemGerada = sgfplib.SetTemplateFormat(SGFDxTemplateFormat.TEMPLATE_FORMAT_ISO19794);
        /**
         *  System.out.println("SetTemplateFormat returned : [" + err + "]");
         */

        /**
         * GET MAX TEMPLATE SIZE FOR ISO19794
         */
    	imagemGerada = sgfplib.GetMaxTemplateSize(maxSize);

        /**
         * MAX TEMPLATE SIZE = System.out.println("Max ISO19794 Template Size is : [" + maxSize[0] + "]")
         */

        /**
         * CREATE ISO19794 TEMPLATE FOR FINGER
         * Call CreateTemplate()
         * CALL GetTemplateSize() TO GET THE SIZE OF CREATED TEMPLATE
         * TEMPLATE SIZE = System.out.println("ISO19794 Template Size is : [" + size[0] + "]");
         */
        ISOminutiaeBuffer1 = new byte[maxSize[0]];
        imagemGerada = sgfplib.CreateTemplate(fingerInfo, imageBuffer1, ISOminutiaeBuffer1);
        imagemGerada = sgfplib.GetTemplateSize(ISOminutiaeBuffer1, size);
        try
        {
            if (err == SGFDxErrorCode.SGFDX_ERROR_NONE)
            {
                /**
                 * salva a digital
                 */
                fout = new FileOutputStream("fingerPrintTemplate.iso19794");
                fp = new PrintStream(fout);
                fp.write(ISOminutiaeBuffer1,0, size[0]);
                fp.close();
                fout.close();
                fp = null;
                fout = null;

                /**
                 * carrega a template da digital para passar pra base64
                 */
                File file = new File("fingerPrintTemplate.iso19794");
                byte[] loadedFile = carregaImagem(file);
                byte[] encodedFile = Base64.getEncoder().encode(loadedFile);
                base64File = new String(encodedFile);

            }
        }
        catch (IOException e)
        {
            /**
             * EXCEPTION
             */
           return new Secugen(false,"Erro ao capturar a digital, tente denovo","Arquivo não foi criado");
        }


        /*** Desliga o led antes de desligar a leitora
         */
        err =sgfplib.SetLedOn(false);
        /**
         * Desliga a leitora
         */
        err = sgfplib.CloseDevice();

        /**
         * Limpa a imagem 
         */
        //
        return new Secugen(true,base64Image,base64File);
    
    
}



/**
 * 
 * Criando a imagem
 */

public  void criaImagem(byte[][] img , String imageName) {
    String path = imageName;
    BufferedImage image = new BufferedImage(img.length, img[0].length, BufferedImage.TYPE_BYTE_GRAY);
    for (int x = 0; x < img.length; x++) {
        for (int y = 0; y <img[0].length; y++) {
            image.setRGB(x, y, img[x][y]);
        }
    }

    File ImageFile = new File(path);
    try {
        ImageIO.write(image, "jpg", ImageFile);
    } catch (IOException e) {
        e.printStackTrace();
    }
}

/**
 * 
 * carregamento da imagem para transformar em base64
 */

private static byte[] carregaImagem(File file) throws IOException {
    InputStream is = new FileInputStream(file);

    long length = file.length();
    if (length > Integer.MAX_VALUE) {
        // arquivo mto grande
    }
    byte[] bytes = new byte[(int)length];
    
    int offset = 0;
    int numRead = 0;
    while (offset < bytes.length
           && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
        offset += numRead;
    }

    if (offset < bytes.length) {
        is.close();
        throw new IOException("Deu erro com o arquivo gerado: "+file.getName());
    }

    is.close();
    return bytes;
}

}
