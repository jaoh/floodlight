/**
 *    Copyright 2013, Big Switch Networks, Inc.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License"); you may
 *    not use this file except in compliance with the License. You may obtain
 *    a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *    WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *    License for the specific language governing permissions and limitations
 *    under the License.
 **/

package net.floodlightcontroller.ui.web;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.restlet.Client;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Protocol;
import org.restlet.resource.Directory;
import org.restlet.routing.Router;
import org.restlet.security.ChallengeAuthenticator;
import org.restlet.security.MapVerifier;

import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.restserver.IRestApiService;
import net.floodlightcontroller.restserver.RestletRoutable;

public class StaticWebRoutable implements RestletRoutable, IFloodlightModule {

	private IRestApiService restApi;
	private Map<String,String> config;

	
    @Override
    public Collection<Class<? extends IFloodlightService>> getModuleDependencies() {
        Collection<Class<? extends IFloodlightService>> l = 
                new ArrayList<Class<? extends IFloodlightService>>();
        l.add(IRestApiService.class);
        return l;
    }
    
    @Override
    public Collection<Class<? extends IFloodlightService>> getModuleServices() {
        return null;
    }
    
    @Override
    public Map<Class<? extends IFloodlightService>, IFloodlightService>
            getServiceImpls() {
        return null;
    }

    @Override
    public void init(FloodlightModuleContext context)
                                             throws FloodlightModuleException {
        restApi = context.getServiceImpl(IRestApiService.class);
    }
    
    @Override
    public void startUp(FloodlightModuleContext context) {
        // Add our REST API
        restApi.addRestletRoutable(this);
        // Get the specfied password and username
        config = context.getConfigParams(this);
    }

	@Override
	public Restlet getRestlet(Context context) {
        Router router = new Router(context);
        Directory dir = new Directory(context, "clap://classloader/web/");
        dir.setIndexName("index.html"); /* redirect from <ip>:<port>/ui/ --> /ui/index.html */
        router.attach("", dir);
        context.setClientDispatcher(new Client(context, Protocol.CLAP));
    
        // Create a simple password verifier
        MapVerifier verifier = new MapVerifier();
        
        verifier.getLocalSecrets().put(this.config.get("username"), this.config.get("password").toCharArray());
        
        // Create a Guard
        ChallengeAuthenticator guard = new ChallengeAuthenticator(
        		context, ChallengeScheme.HTTP_BASIC, "Enter login");
        guard.setVerifier(verifier);
        
        // Create a Directory able to return a deep hierarchy of files
        //dir.setListingAllowed(true);
        guard.setNext(dir);
        return guard;

        }

	@Override
	public String basePath() {
		return "/ui/";
	}

}
