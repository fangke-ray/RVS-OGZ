

/**
 * RVS005_RVS2SFDC_Syn_OutService.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.2  Built on : Apr 17, 2012 (05:33:49 IST)
 */

    package global.olympus.cna.rvs.rvs005;

    /*
     *  RVS005_RVS2SFDC_Syn_OutService java interface
     */

    public interface RVS005_RVS2SFDC_Syn_OutService {
          

        /**
          * Auto generated method signature
          * 
                    * @param mT_RVS0050
                
         */

         
                     public global.olympus.cna.rvs.rvs005.MT_RVS005_Return RVS005_RVS2SFDC_Syn_Out(

                        global.olympus.cna.rvs.rvs005.MT_RVS005 mT_RVS0050)
                        throws java.rmi.RemoteException
             ;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param mT_RVS0050
            
          */
        public void startRVS005_RVS2SFDC_Syn_Out(

            global.olympus.cna.rvs.rvs005.MT_RVS005 mT_RVS0050,

            final global.olympus.cna.rvs.rvs005.RVS005_RVS2SFDC_Syn_OutServiceCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        
       //
       }
    