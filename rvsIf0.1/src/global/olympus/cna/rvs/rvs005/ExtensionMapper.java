
/**
 * ExtensionMapper.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.2  Built on : Apr 17, 2012 (05:34:40 IST)
 */

        
            package global.olympus.cna.rvs.rvs005;
        
            /**
            *  ExtensionMapper class
            */
            @SuppressWarnings({"unchecked","unused"})
        
        public  class ExtensionMapper{

          public static java.lang.Object getTypeObject(java.lang.String namespaceURI,
                                                       java.lang.String typeName,
                                                       javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception{

              
                  if (
                  "http://cna.olympus.global/rvs/RVS005".equals(namespaceURI) &&
                  "Monitoring".equals(typeName)){
                   
                            return  global.olympus.cna.rvs.rvs005.Monitoring.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://cna.olympus.global/rvs/RVS005".equals(namespaceURI) &&
                  "InspectionResult_type0".equals(typeName)){
                   
                            return  global.olympus.cna.rvs.rvs005.InspectionResult_type0.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://cna.olympus.global/rvs/RVS005".equals(namespaceURI) &&
                  "DT_RVS005".equals(typeName)){
                   
                            return  global.olympus.cna.rvs.rvs005.DT_RVS005.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://cna.olympus.global/rvs/RVS005".equals(namespaceURI) &&
                  "DT_RVS005_Return".equals(typeName)){
                   
                            return  global.olympus.cna.rvs.rvs005.DT_RVS005_Return.Factory.parse(reader);
                        

                  }

              
             throw new org.apache.axis2.databinding.ADBException("Unsupported type " + namespaceURI + " " + typeName);
          }

        }
    