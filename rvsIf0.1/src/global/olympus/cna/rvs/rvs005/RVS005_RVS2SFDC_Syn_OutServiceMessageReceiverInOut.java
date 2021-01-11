
/**
 * RVS005_RVS2SFDC_Syn_OutServiceMessageReceiverInOut.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.2  Built on : Apr 17, 2012 (05:33:49 IST)
 */
        package global.olympus.cna.rvs.rvs005;

        /**
        *  RVS005_RVS2SFDC_Syn_OutServiceMessageReceiverInOut message receiver
        */

        public class RVS005_RVS2SFDC_Syn_OutServiceMessageReceiverInOut extends org.apache.axis2.receivers.AbstractInOutMessageReceiver{


        public void invokeBusinessLogic(org.apache.axis2.context.MessageContext msgContext, org.apache.axis2.context.MessageContext newMsgContext)
        throws org.apache.axis2.AxisFault{

        try {

        // get the implementation class for the Web Service
        Object obj = getTheImplementationObject(msgContext);

        RVS005_RVS2SFDC_Syn_OutServiceSkeletonInterface skel = (RVS005_RVS2SFDC_Syn_OutServiceSkeletonInterface)obj;
        //Out Envelop
        org.apache.axiom.soap.SOAPEnvelope envelope = null;
        //Find the axisOperation that has been set by the Dispatch phase.
        org.apache.axis2.description.AxisOperation op = msgContext.getOperationContext().getAxisOperation();
        if (op == null) {
        throw new org.apache.axis2.AxisFault("Operation is not located, if this is doclit style the SOAP-ACTION should specified via the SOAP Action to use the RawXMLProvider");
        }

        java.lang.String methodName;
        if((op.getName() != null) && ((methodName = org.apache.axis2.util.JavaUtils.xmlNameToJavaIdentifier(op.getName().getLocalPart())) != null)){


        

            if("rVS005_RVS2SFDC_Syn_Out".equals(methodName)){
                
                global.olympus.cna.rvs.rvs005.MT_RVS005_Return mT_RVS005_Return3 = null;
	                        global.olympus.cna.rvs.rvs005.MT_RVS005 wrappedParam =
                                                             (global.olympus.cna.rvs.rvs005.MT_RVS005)fromOM(
                                    msgContext.getEnvelope().getBody().getFirstElement(),
                                    global.olympus.cna.rvs.rvs005.MT_RVS005.class,
                                    getEnvelopeNamespaces(msgContext.getEnvelope()));
                                                
                                               mT_RVS005_Return3 =
                                                   
                                                   
                                                         skel.rVS005_RVS2SFDC_Syn_Out(wrappedParam)
                                                    ;
                                            
                                        envelope = toEnvelope(getSOAPFactory(msgContext), mT_RVS005_Return3, false, new javax.xml.namespace.QName("http://cna.olympus.global/rvs/RVS005",
                                                    "rVS005_RVS2SFDC_Syn_Out"));
                                    
            } else {
              throw new java.lang.RuntimeException("method not found");
            }
        

        newMsgContext.setEnvelope(envelope);
        }
        }
        catch (java.lang.Exception e) {
        throw org.apache.axis2.AxisFault.makeFault(e);
        }
        }
        
        //
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
        
                    private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory, global.olympus.cna.rvs.rvs005.MT_RVS005_Return param, boolean optimizeContent, javax.xml.namespace.QName methodQName)
                        throws org.apache.axis2.AxisFault{
                      try{
                          org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                           
                                    emptyEnvelope.getBody().addChild(param.getOMElement(global.olympus.cna.rvs.rvs005.MT_RVS005_Return.MY_QNAME,factory));
                                

                         return emptyEnvelope;
                    } catch(org.apache.axis2.databinding.ADBException e){
                        throw org.apache.axis2.AxisFault.makeFault(e);
                    }
                    }
                    
                         private global.olympus.cna.rvs.rvs005.MT_RVS005_Return wrapRVS005_RVS2SFDC_Syn_Out(){
                                global.olympus.cna.rvs.rvs005.MT_RVS005_Return wrappedElement = new global.olympus.cna.rvs.rvs005.MT_RVS005_Return();
                                return wrappedElement;
                         }
                    


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

        private org.apache.axis2.AxisFault createAxisFault(java.lang.Exception e) {
        org.apache.axis2.AxisFault f;
        Throwable cause = e.getCause();
        if (cause != null) {
            f = new org.apache.axis2.AxisFault(e.getMessage(), cause);
        } else {
            f = new org.apache.axis2.AxisFault(e.getMessage());
        }

        return f;
    }

        }//end of class
    