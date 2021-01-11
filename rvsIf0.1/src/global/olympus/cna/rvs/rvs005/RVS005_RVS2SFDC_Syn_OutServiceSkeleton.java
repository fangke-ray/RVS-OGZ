/**
 * RVS005_RVS2SFDC_Syn_OutServiceSkeleton.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.2  Built on : Apr 17, 2012 (05:33:49 IST)
 */
package global.olympus.cna.rvs.rvs005;

/**
 * RVS005_RVS2SFDC_Syn_OutServiceSkeleton java skeleton for the axisService
 */
public class RVS005_RVS2SFDC_Syn_OutServiceSkeleton implements
		RVS005_RVS2SFDC_Syn_OutServiceSkeletonInterface {

	/**
	 * Auto generated method signature
	 * 
	 * @param mT_RVS0050
	 * @return mT_RVS005_Return1
	 */

	public global.olympus.cna.rvs.rvs005.MT_RVS005_Return rVS005_RVS2SFDC_Syn_Out(
			global.olympus.cna.rvs.rvs005.MT_RVS005 mT_RVS0050) {
		MT_RVS005_Return ret = new MT_RVS005_Return();
		DT_RVS005_Return dt = new DT_RVS005_Return();
		dt.setErrItem("");
		dt.setErrMsg("");
		dt.setStatus("OK");
		ret.setMT_RVS005_Return(dt);
		return ret;
	}

}
