package gov.noaa.eds.service;

import thredds.server.metadata.bean.Extent;
import thredds.server.metadata.bean.MetadataContainer;
import thredds.server.metadata.util.ElementNameComparator;
import thredds.server.metadata.util.NCMLModifier;
import thredds.server.metadata.util.ThreddsExtentUtil;
import thredds.server.metadata.util.ThreddsTranslatorUtil;
import thredds.server.metadata.util.XMLUtil;
import gov.noaa.eds.util.FileUtility;

import java.io.File;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.jdom.Element;

public class WafService {

	private static Logger logger = Logger.getLogger(WafService.class);

	// rawgit is a CDN for github hosted files.
	private static final String _xsltMetadataAssessmentUrl = "https://cdn.rawgit.com/NOAA-PMEL/uafnciso/master/transforms/UnidataDDCount-HTML.xsl";
	private static final String _xsltIsoUrl = "https://cdn.rawgit.com/NOAA-PMEL/uafnciso/master/transforms/UnidataDD2MI.xsl";
    
    public static Vector<String> generateNcml(final Vector<MetadataContainer> mdcs, final Vector<String> ncmlFiles, final String wafRoot){
		FileUtility wafNcmlDir = new FileUtility(wafRoot);
		wafNcmlDir.mkdirs();
        try {
            for(MetadataContainer mdc:mdcs) {
            	String urlStr = mdc.getOpenDapUrl();
            	int startPos = urlStr.indexOf("/", 7);
            	String urlSrcRoot = urlStr.substring(startPos+1, urlStr.lastIndexOf("/")+1);
            	String baseFileNm = urlStr.substring(urlStr.lastIndexOf("/")+1, urlStr.length());
            	String ncmlFileStr = urlSrcRoot.replace('/', '_') + baseFileNm;
            	int pos = ncmlFileStr.indexOf(".");            	
            	if (pos>-1) { //file
            		ncmlFileStr = ncmlFileStr.substring(0, pos) + ".xml";
            	} else { //aggregation
            		ncmlFileStr = ncmlFileStr + ".xml";
                }
            	
            	String ncmlFilePath  = wafRoot + ncmlFileStr;
            	logger.info("ncmlFilePath=" + ncmlFilePath);
				File ncmlFile = ThreddsTranslatorUtil.getNcml(urlStr, ncmlFilePath);
				if (ncmlFile!=null) {
					ncmlFiles.add(ncmlFilePath);
				    Extent ext = ThreddsExtentUtil.getExtent(urlStr);
		    	    XMLUtil xmlUtil = new XMLUtil(ncmlFilePath);
		    	    List<Element> childElems = xmlUtil.elemFinder("//ncml:attribute", "ncml", "http://www.unidata.ucar.edu/namespaces/netcdf/ncml-2.2");		    	
		    	    NCMLModifier ncmlMod = new NCMLModifier();	    	
		    	    List<Element> list = xmlUtil.elemFinder("//ncml:netcdf", "ncml", "http://www.unidata.ucar.edu/namespaces/netcdf/ncml-2.2");
		    	    
		    	    Element rootElem = list.get(0);
		    	    Element cfGroupElem = ncmlMod.doAddGroupElem(rootElem, "CFMetadata");
		    	    ncmlMod.addCFMetadata(ext, cfGroupElem);

		    	    Element threddsGroupElem = ncmlMod.doAddGroupElem(rootElem, "THREDDSMetadata");
		    	    ncmlMod.addThreddsMetadata(mdc.getDataset(), threddsGroupElem);
		    	    
		    	    Element ncisoGroupElem = ncmlMod.doAddGroupElem(rootElem, "NCISOMetadata");
		    	    ncmlMod.addNcIsoMetadata(ncisoGroupElem);
		    	    
		    	    xmlUtil.sortElements(rootElem, new ElementNameComparator());
		    	    xmlUtil.write(ncmlFilePath);       
				}
            }
        } catch (Exception e) {
        	logger.error(e);
        }
        return ncmlFiles;
    }
    
    public static void generateNcmlRubric(final Vector<String> files, final String wafDirStr){
		FileUtility wafDirFu = new FileUtility(wafDirStr);
		wafDirFu.mkdirs();
        try {
            for(String ncmlFilePathStr:files) {
            	int startPos = ncmlFilePathStr.lastIndexOf("/");
            	String reportFileNm = ncmlFilePathStr.substring(startPos+1, ncmlFilePathStr.length()-3) + "html";
            	ThreddsTranslatorUtil.transform(_xsltMetadataAssessmentUrl, ncmlFilePathStr, wafDirStr + reportFileNm);
            }
        } catch (Exception e) {
        	logger.error(e);
        }
    }

    public static void generateIso(final Vector<String> files, final String wafDirStr){
		FileUtility wafDirFu = new FileUtility(wafDirStr);
		wafDirFu.mkdirs();
        try {
            for(String ncmlFilePathStr:files) {
            	int startPos = ncmlFilePathStr.lastIndexOf("/");
            	String reportFileNm = ncmlFilePathStr.substring(startPos+1, ncmlFilePathStr.length()-3) + "xml";
            	ThreddsTranslatorUtil.transform(_xsltIsoUrl, ncmlFilePathStr, wafDirStr + reportFileNm);
            }
        } catch (Exception e) {
        	logger.error(e);
        }
    }
    
}
