
/**
 * RVS005_RVS2SFDC_Syn_OutServiceStub.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.2  Built on : Apr 17, 2012 (05:33:49 IST)
 */
        package global.olympus.cna.rvs.rvs005;

import org.apache.log4j.Logger;

        

        /*
        *  RVS005_RVS2SFDC_Syn_OutServiceStub java implementation
        */

        
        public class RVS005_RVS2SFDC_Syn_OutServiceStub extends org.apache.axis2.client.Stub
        implements RVS005_RVS2SFDC_Syn_OutService{
        protected org.apache.axis2.description.AxisOperation[] _operations;
    	static Logger _logger = Logger.getLogger("TriggerServlet");

        //hashmaps to keep the fault mapping
        private java.util.HashMap faultExceptionNameMap = new java.util.HashMap();
        private java.util.HashMap faultExceptionClassNameMap = new java.util.HashMap();
        private java.util.HashMap faultMessageMap = new java.util.HashMap();

        private static int counter = 0;

        private static synchronized java.lang.String getUniqueSuffix(){
            // reset the counter if it is greater than 99999
            if (counter > 99999){
                counter = 0;
            }
            counter = counter + 1; 
            return java.lang.Long.toString(java.lang.System.currentTimeMillis()) + "_" + counter;
        }

    
    private void populateAxisService() throws org.apache.axis2.AxisFault {

     //creating the Service with a unique name
     _service = new org.apache.axis2.description.AxisService("RVS005_RVS2SFDC_Syn_OutService" + getUniqueSuffix());
     addAnonymousOperations();

        //creating the operations
        org.apache.axis2.description.AxisOperation __operation;

        _operations = new org.apache.axis2.description.AxisOperation[1];
        
                   __operation = new org.apache.axis2.description.OutInAxisOperation();
                

            __operation.setName(new javax.xml.namespace.QName("http://cna.olympus.global/rvs/RVS005", "RVS005_RVS2SFDC_Syn_Out"));
	    _service.addOperation(__operation);
	    

	    
	    
            _operations[0]=__operation;
            
        
        }

    //populates the faults
    private void populateFaults(){
         


    }

    /**
      *Constructor that takes in a configContext
      */

    public RVS005_RVS2SFDC_Syn_OutServiceStub(org.apache.axis2.context.ConfigurationContext configurationContext,
       java.lang.String targetEndpoint)
       throws org.apache.axis2.AxisFault {
         this(configurationContext,targetEndpoint,false);
   }


   /**
     * Constructor that takes in a configContext  and useseperate listner
     */
   public RVS005_RVS2SFDC_Syn_OutServiceStub(org.apache.axis2.context.ConfigurationContext configurationContext,
        java.lang.String targetEndpoint, boolean useSeparateListener)
        throws org.apache.axis2.AxisFault {
         //To populate AxisService
         populateAxisService();
         populateFaults();

        _serviceClient = new org.apache.axis2.client.ServiceClient(configurationContext,_service);
        
	
        _serviceClient.getOptions().setTo(new org.apache.axis2.addressing.EndpointReference(
                targetEndpoint));
        _serviceClient.getOptions().setUseSeparateListener(useSeparateListener);
        
    
    }

    /**
     * Default Constructor
     */
    public RVS005_RVS2SFDC_Syn_OutServiceStub(org.apache.axis2.context.ConfigurationContext configurationContext) throws org.apache.axis2.AxisFault {
        
                    this(configurationContext,"http://vsappoq-sh03.cna.olympus.global:50000/XISOAPAdapter/MessageServlet?senderParty=&senderService=RVS&receiverParty=&receiverService=&interface=RVS005_RVS2SFDC_Syn_Out&interfaceNamespace=http%3A%2F%2Fcna.olympus.global%2Frvs%2FRVS005" );
                
    }

    /**
     * Default Constructor
     */
    public RVS005_RVS2SFDC_Syn_OutServiceStub() throws org.apache.axis2.AxisFault {
        
                    this("http://vsappoq-sh03.cna.olympus.global:50000/XISOAPAdapter/MessageServlet?senderParty=&senderService=RVS&receiverParty=&receiverService=&interface=RVS005_RVS2SFDC_Syn_Out&interfaceNamespace=http%3A%2F%2Fcna.olympus.global%2Frvs%2FRVS005" );
                
    }

    /**
     * Constructor taking the target endpoint
     */
    public RVS005_RVS2SFDC_Syn_OutServiceStub(java.lang.String targetEndpoint) throws org.apache.axis2.AxisFault {
        this(null,targetEndpoint);
    }



        
	/**
	 * Auto generated method signature
	 * 
	 * @see global.olympus.cna.rvs.rvs005.RVS005_RVS2SFDC_Syn_OutService#RVS005_RVS2SFDC_Syn_Out
	 * @param mT_RVS0052
	 */

	public global.olympus.cna.rvs.rvs005.MT_RVS005_Return RVS005_RVS2SFDC_Syn_Out(
			global.olympus.cna.rvs.rvs005.MT_RVS005 mT_RVS0052)
	throws java.rmi.RemoteException
                    
	{
		org.apache.axis2.context.MessageContext _messageContext = null;
		try {
			org.apache.axis2.client.OperationClient _operationClient = _serviceClient
					.createClient(_operations[0].getName());
			_operationClient.getOptions().setAction("http://sap.com/xi/WebService/soap1.1");
			_operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(
					true);

			addPropertyToOperationClient(_operationClient,
					org.apache.axis2.description.WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR,
					"&");

			// create a message context
			_messageContext = new org.apache.axis2.context.MessageContext();

			// create SOAP envelope with that payload
			org.apache.axiom.soap.SOAPEnvelope env = null;

			env = toEnvelope(getFactory(_operationClient.getOptions().getSoapVersionURI()), mT_RVS0052,
					optimizeContent(new javax.xml.namespace.QName(
							"http://cna.olympus.global/rvs/RVS005",
							"RVS005_RVS2SFDC_Syn_Out")),
					new javax.xml.namespace.QName(
							"http://cna.olympus.global/rvs/RVS005",
							"RVS005_RVS2SFDC_Syn_Out"));

			// adding SOAP soap_headers
			_serviceClient.addHeadersToEnvelope(env);
			// set the message context with that soap envelope
			_messageContext.setEnvelope(env);

			// add the message contxt to the operation client
			_operationClient.addMessageContext(_messageContext);

			_logger.info("EnvelopeBody");
			_logger.info(env.getBody().toString());
			_logger.info(_messageContext.getEnvelope().getBody().toString());

			// execute the operation client
			_operationClient.execute(true);

			org.apache.axis2.context.MessageContext _returnMessageContext = _operationClient
					.getMessageContext(org.apache.axis2.wsdl.WSDLConstants.MESSAGE_LABEL_IN_VALUE);
			org.apache.axiom.soap.SOAPEnvelope _returnEnv = _returnMessageContext
					.getEnvelope();

			_logger.info("RetEnvelopeBody");
			_logger.info(_returnEnv.getBody().toString());

			java.lang.Object object = fromOM(_returnEnv.getBody()
					.getFirstElement(),
					global.olympus.cna.rvs.rvs005.MT_RVS005_Return.class,
					getEnvelopeNamespaces(_returnEnv));

			return (global.olympus.cna.rvs.rvs005.MT_RVS005_Return) object;

		} catch (org.apache.axis2.AxisFault f) {

			org.apache.axiom.om.OMElement faultElt = f.getDetail();
			if (faultElt != null) {
				if (faultExceptionNameMap
						.containsKey(new org.apache.axis2.client.FaultMapKey(
								faultElt.getQName(), "RVS005_RVS2SFDC_Syn_Out"))) {
					// make the fault by reflection
					try {
						java.lang.String exceptionClassName = (java.lang.String) faultExceptionClassNameMap
								.get(new org.apache.axis2.client.FaultMapKey(
										faultElt.getQName(),
										"RVS005_RVS2SFDC_Syn_Out"));
						java.lang.Class exceptionClass = java.lang.Class
								.forName(exceptionClassName);
						java.lang.reflect.Constructor constructor = exceptionClass
								.getConstructor(String.class);
						java.lang.Exception ex = (java.lang.Exception) constructor
								.newInstance(f.getMessage());
						// message class
						java.lang.String messageClassName = (java.lang.String) faultMessageMap
								.get(new org.apache.axis2.client.FaultMapKey(
										faultElt.getQName(),
										"RVS005_RVS2SFDC_Syn_Out"));
						java.lang.Class messageClass = java.lang.Class
								.forName(messageClassName);
						java.lang.Object messageObject = fromOM(faultElt,
								messageClass, null);
						java.lang.reflect.Method m = exceptionClass.getMethod(
								"setFaultMessage",
								new java.lang.Class[] { messageClass });
						m.invoke(ex, new java.lang.Object[] { messageObject });

						throw new java.rmi.RemoteException(ex.getMessage(), ex);
					} catch (java.lang.ClassCastException e) {
						// we cannot intantiate the class - throw the original
						// Axis fault
						throw f;
					} catch (java.lang.ClassNotFoundException e) {
						// we cannot intantiate the class - throw the original
						// Axis fault
						throw f;
					} catch (java.lang.NoSuchMethodException e) {
						// we cannot intantiate the class - throw the original
						// Axis fault
						throw f;
					} catch (java.lang.reflect.InvocationTargetException e) {
						// we cannot intantiate the class - throw the original
						// Axis fault
						throw f;
					} catch (java.lang.IllegalAccessException e) {
						// we cannot intantiate the class - throw the original
						// Axis fault
						throw f;
					} catch (java.lang.InstantiationException e) {
						// we cannot intantiate the class - throw the original
						// Axis fault
						throw f;
					}
				} else {
					throw f;
				}
			} else {
				throw f;
			}
		} finally {
			if (_messageContext.getTransportOut() != null) {
				_messageContext.getTransportOut().getSender()
						.cleanup(_messageContext);
			}
		}
	}
            
                /**
                * Auto generated method signature for Asynchronous Invocations
                * 
                * @see global.olympus.cna.rvs.rvs005.RVS005_RVS2SFDC_Syn_OutService#startRVS005_RVS2SFDC_Syn_Out
                    * @param mT_RVS0052
                
                */
                public  void startRVS005_RVS2SFDC_Syn_Out(

                 global.olympus.cna.rvs.rvs005.MT_RVS005 mT_RVS0052,

                  final global.olympus.cna.rvs.rvs005.RVS005_RVS2SFDC_Syn_OutServiceCallbackHandler callback)

                throws java.rmi.RemoteException{

              org.apache.axis2.client.OperationClient _operationClient = _serviceClient.createClient(_operations[0].getName());
             _operationClient.getOptions().setAction("http://sap.com/xi/WebService/soap1.1");
             _operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);

              
              
                  addPropertyToOperationClient(_operationClient,org.apache.axis2.description.WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR,"&");
              


              // create SOAP envelope with that payload
              org.apache.axiom.soap.SOAPEnvelope env=null;
              final org.apache.axis2.context.MessageContext _messageContext = new org.apache.axis2.context.MessageContext();

                    
                                    //Style is Doc.
                                    
                                                    
                                                    env = toEnvelope(getFactory(_operationClient.getOptions().getSoapVersionURI()),
                                                    mT_RVS0052,
                                                    optimizeContent(new javax.xml.namespace.QName("http://cna.olympus.global/rvs/RVS005",
                                                    "RVS005_RVS2SFDC_Syn_Out")), new javax.xml.namespace.QName("http://cna.olympus.global/rvs/RVS005",
                                                    "RVS005_RVS2SFDC_Syn_Out"));
                                                
        // adding SOAP soap_headers
         _serviceClient.addHeadersToEnvelope(env);
        // create message context with that soap envelope
        _messageContext.setEnvelope(env);

        // add the message context to the operation client
        _operationClient.addMessageContext(_messageContext);


                    
        _operationClient.setCallback(new org.apache.axis2.client.async.AxisCallback() {
            public void onMessage(org.apache.axis2.context.MessageContext resultContext) {
            	_logger.info("callback message start");
            try {
                org.apache.axiom.soap.SOAPEnvelope resultEnv = resultContext.getEnvelope();
                
                        java.lang.Object object = fromOM(resultEnv.getBody().getFirstElement(),
                                                         global.olympus.cna.rvs.rvs005.MT_RVS005_Return.class,
                                                         getEnvelopeNamespaces(resultEnv));
                        callback.receiveResultRVS005_RVS2SFDC_Syn_Out(
                        (global.olympus.cna.rvs.rvs005.MT_RVS005_Return)object);
                        
            } catch (org.apache.axis2.AxisFault e) {
                callback.receiveErrorRVS005_RVS2SFDC_Syn_Out(e);
            }
            	_logger.info("callback message end");
            }

            public void onError(java.lang.Exception error) {
            	_logger.error("callback error start");

            	if (error instanceof org.apache.axis2.AxisFault) {
					org.apache.axis2.AxisFault f = (org.apache.axis2.AxisFault) error;
					org.apache.axiom.om.OMElement faultElt = f.getDetail();
					if (faultElt!=null){
						if (faultExceptionNameMap.containsKey(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"RVS005_RVS2SFDC_Syn_Out"))){
							//make the fault by reflection
							try{
									java.lang.String exceptionClassName = (java.lang.String)faultExceptionClassNameMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"RVS005_RVS2SFDC_Syn_Out"));
									java.lang.Class exceptionClass = java.lang.Class.forName(exceptionClassName);
									java.lang.reflect.Constructor constructor = exceptionClass.getConstructor(String.class);
                                    java.lang.Exception ex = (java.lang.Exception) constructor.newInstance(f.getMessage());
									//message class
									java.lang.String messageClassName = (java.lang.String)faultMessageMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"RVS005_RVS2SFDC_Syn_Out"));
										java.lang.Class messageClass = java.lang.Class.forName(messageClassName);
									java.lang.Object messageObject = fromOM(faultElt,messageClass,null);
									java.lang.reflect.Method m = exceptionClass.getMethod("setFaultMessage",
											new java.lang.Class[]{messageClass});
									m.invoke(ex,new java.lang.Object[]{messageObject});
									
	
						            callback.receiveErrorRVS005_RVS2SFDC_Syn_Out(new java.rmi.RemoteException(ex.getMessage(), ex));
                            } catch(java.lang.ClassCastException e){
                                // we cannot intantiate the class - throw the original Axis fault
                                callback.receiveErrorRVS005_RVS2SFDC_Syn_Out(f);
                            } catch (java.lang.ClassNotFoundException e) {
                                // we cannot intantiate the class - throw the original Axis fault
                                callback.receiveErrorRVS005_RVS2SFDC_Syn_Out(f);
                            } catch (java.lang.NoSuchMethodException e) {
                                // we cannot intantiate the class - throw the original Axis fault
                                callback.receiveErrorRVS005_RVS2SFDC_Syn_Out(f);
                            } catch (java.lang.reflect.InvocationTargetException e) {
                                // we cannot intantiate the class - throw the original Axis fault
                                callback.receiveErrorRVS005_RVS2SFDC_Syn_Out(f);
                            } catch (java.lang.IllegalAccessException e) {
                                // we cannot intantiate the class - throw the original Axis fault
                                callback.receiveErrorRVS005_RVS2SFDC_Syn_Out(f);
                            } catch (java.lang.InstantiationException e) {
                                // we cannot intantiate the class - throw the original Axis fault
                                callback.receiveErrorRVS005_RVS2SFDC_Syn_Out(f);
                            } catch (org.apache.axis2.AxisFault e) {
                                // we cannot intantiate the class - throw the original Axis fault
                                callback.receiveErrorRVS005_RVS2SFDC_Syn_Out(f);
                            }
					    } else {
						    callback.receiveErrorRVS005_RVS2SFDC_Syn_Out(f);
					    }
					} else {
					    callback.receiveErrorRVS005_RVS2SFDC_Syn_Out(f);
					}
				} else {
				    callback.receiveErrorRVS005_RVS2SFDC_Syn_Out(error);
				}

            	_logger.error("callback error end");
            }

            public void onFault(org.apache.axis2.context.MessageContext faultContext) {
            	_logger.error("callback fault start");
                org.apache.axis2.AxisFault fault = org.apache.axis2.util.Utils.getInboundFaultFromMessageContext(faultContext);
                onError(fault);
            	_logger.error("callback fault end");
            }

            public void onComplete() {
            	_logger.error("callback complete start");
                try {
                    _messageContext.getTransportOut().getSender().cleanup(_messageContext);
                } catch (org.apache.axis2.AxisFault axisFault) {
                    callback.receiveErrorRVS005_RVS2SFDC_Syn_Out(axisFault);
                }
            	_logger.error("callback complete end");
            }
        });
                        

          org.apache.axis2.util.CallbackReceiver _callbackReceiver = null;
        if ( _operations[0].getMessageReceiver()==null &&  _operationClient.getOptions().isUseSeparateListener()) {
           _callbackReceiver = new org.apache.axis2.util.CallbackReceiver();
          _operations[0].setMessageReceiver(
                    _callbackReceiver);
        }

           //execute the operation client
           _operationClient.execute(false);

                    }
                


       /**
        *  A utility method that copies the namepaces from the SOAPEnvelope
        */
       private java.util.Map getEnvelopeNamespaces(org.apache.axiom.soap.SOAPEnvelope env){
        java.util.Map returnMap = new java.util.HashMap();
        java.util.Iterator namespaceIterator = env.getAllDeclaredNamespaces();
        while (namespaceIterator.hasNext()) {
            org.apache.axiom.om.OMNamespace ns = (org.apache.axiom.om.OMNamespace) namespaceIterator.next();
            returnMap.put(ns.getPrefix(),ns.getNamespaceURI());
        }
       return returnMap;
    }

    
    
    private javax.xml.namespace.QName[] opNameArray = null;
    private boolean optimizeContent(javax.xml.namespace.QName opName) {
        

        if (opNameArray == null) {
            return false;
        }
        for (int i = 0; i < opNameArray.length; i++) {
            if (opName.equals(opNameArray[i])) {
                return true;   
            }
        }
        return false;
    }
     //http://vsappoq-sh03.cna.olympus.global:50000/XISOAPAdapter/MessageServlet?senderParty=&senderService=RVS&receiverParty=&receiverService=&interface=RVS005_RVS2SFDC_Syn_Out&interfaceNamespace=http%3A%2F%2Fcna.olympus.global%2Frvs%2FRVS005
            private  org.apache.axiom.om.OMElement  toOM(global.olympus.cna.rvs.rvs005.MT_RVS005 param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(global.olympus.cna.rvs.rvs005.MT_RVS005.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(global.olympus.cna.rvs.rvs005.MT_RVS005_Return param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(global.olympus.cna.rvs.rvs005.MT_RVS005_Return.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
                                    
                                        private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory, global.olympus.cna.rvs.rvs005.MT_RVS005 param, boolean optimizeContent, javax.xml.namespace.QName methodQName)
                                        throws org.apache.axis2.AxisFault{

                                             
                                                    try{

                                                            org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                                                            emptyEnvelope.getBody().addChild(param.getOMElement(global.olympus.cna.rvs.rvs005.MT_RVS005.MY_QNAME,factory));
                                                            return emptyEnvelope;
                                                        } catch(org.apache.axis2.databinding.ADBException e){
                                                            throw org.apache.axis2.AxisFault.makeFault(e);
                                                        }
                                                

                                        }
                                
                             
                             /* methods to provide back word compatibility */

                             


        /**
        *  get the default envelope
        */
        private org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory){
        return factory.getDefaultEnvelope();
        }


        private  java.lang.Object fromOM(
        org.apache.axiom.om.OMElement param,
        java.lang.Class type,
        java.util.Map extraNamespaces) throws org.apache.axis2.AxisFault{

        try {
        
                if (global.olympus.cna.rvs.rvs005.MT_RVS005.class.equals(type)){
                
                           return global.olympus.cna.rvs.rvs005.MT_RVS005.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (global.olympus.cna.rvs.rvs005.MT_RVS005_Return.class.equals(type)){
                
                           return global.olympus.cna.rvs.rvs005.MT_RVS005_Return.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
        } catch (java.lang.Exception e) {
        throw org.apache.axis2.AxisFault.makeFault(e);
        }
           return null;
        }


   
   }
   