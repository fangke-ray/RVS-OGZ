
/**
 * RVS005_RVS2SFDC_Syn_OutServiceCallbackHandler.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.2  Built on : Apr 17, 2012 (05:33:49 IST)
 */

    package global.olympus.cna.rvs.rvs005;

    /**
     *  RVS005_RVS2SFDC_Syn_OutServiceCallbackHandler Callback class, Users can extend this class and implement
     *  their own receiveResult and receiveError methods.
     */
    public abstract class RVS005_RVS2SFDC_Syn_OutServiceCallbackHandler{



    protected Object clientData;

    /**
    * User can pass in any object that needs to be accessed once the NonBlocking
    * Web service call is finished and appropriate method of this CallBack is called.
    * @param clientData Object mechanism by which the user can pass in user data
    * that will be avilable at the time this callback is called.
    */
    public RVS005_RVS2SFDC_Syn_OutServiceCallbackHandler(Object clientData){
        this.clientData = clientData;
    }

    /**
    * Please use this constructor if you don't want to set any clientData
    */
    public RVS005_RVS2SFDC_Syn_OutServiceCallbackHandler(){
        this.clientData = null;
    }

    /**
     * Get the client data
     */

     public Object getClientData() {
        return clientData;
     }

        
           /**
            * auto generated Axis2 call back method for RVS005_RVS2SFDC_Syn_Out method
            * override this method for handling normal response from RVS005_RVS2SFDC_Syn_Out operation
            */
           public void receiveResultRVS005_RVS2SFDC_Syn_Out(
                    global.olympus.cna.rvs.rvs005.MT_RVS005_Return result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from RVS005_RVS2SFDC_Syn_Out operation
           */
            public void receiveErrorRVS005_RVS2SFDC_Syn_Out(java.lang.Exception e) {
            }
                


    }
    