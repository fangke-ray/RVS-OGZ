package com.osh.rvs.form;

import java.util.ArrayList;
import java.util.List;

import org.apache.struts.action.ActionForm;
import org.apache.struts.upload.FormFile;

public class UploadForm extends ActionForm {

	private static final long serialVersionUID = 2546637555687213767L;

	/** file property */
	private FormFile file;

	private List<FormFile> files = new ArrayList<FormFile>();

	public FormFile getFile() {
		return file;
	}

	public void setFile(FormFile file) {
		this.file = file;
	}

	public List<FormFile> getFiles() {
		return files;
	}

	public void setFiles(List<FormFile> files) {
		this.files = files;
	}

}
