package edu.utexas.tacc.wcs.filemanager.service.resources;

import java.util.List;

import org.restlet.resource.Get;

import edu.utexas.tacc.wcs.filemanager.common.model.User;

public interface PartnersResource {

	@Get
	public abstract List<User> findProjectPartners();

}