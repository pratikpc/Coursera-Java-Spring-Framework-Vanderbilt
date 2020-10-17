/*
 * 
 * Copyright 2014 Jules White
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */

package org.magnum.mobilecloud.video;

import com.google.common.collect.Lists;
import org.magnum.mobilecloud.video.repository.Video;
import org.magnum.mobilecloud.video.repository.VideoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Arrays;
import java.util.Collection;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
class NotFoundException extends RuntimeException{}

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
class BadRequestException extends RuntimeException{}


@Controller
@RequestMapping(value = "/video")
public class VideoController {
	
	/**
	 * You will need to create one or more Spring controllers to fulfill the
	 * requirements of the assignment. If you use this file, please rename it
	 * to something other than "AnEmptyController"
	 * 
	 * 
		 ________  ________  ________  ________          ___       ___  ___  ________  ___  __       
		|\   ____\|\   __  \|\   __  \|\   ___ \        |\  \     |\  \|\  \|\   ____\|\  \|\  \     
		\ \  \___|\ \  \|\  \ \  \|\  \ \  \_|\ \       \ \  \    \ \  \\\  \ \  \___|\ \  \/  /|_   
		 \ \  \  __\ \  \\\  \ \  \\\  \ \  \ \\ \       \ \  \    \ \  \\\  \ \  \    \ \   ___  \  
		  \ \  \|\  \ \  \\\  \ \  \\\  \ \  \_\\ \       \ \  \____\ \  \\\  \ \  \____\ \  \\ \  \ 
		   \ \_______\ \_______\ \_______\ \_______\       \ \_______\ \_______\ \_______\ \__\\ \__\
		    \|_______|\|_______|\|_______|\|_______|        \|_______|\|_______|\|_______|\|__| \|__|
                                                                                                                                                                                                                                                                        
	 * 
	 */

	@Autowired
	VideoRepository videoRepository;
	
	@RequestMapping(value="/go",method=RequestMethod.GET)
	public @ResponseBody String goodLuck(){
		return "Good Luck!";
	}

	@RequestMapping(method=RequestMethod.GET)
	public @ResponseBody
	Collection<Video> getAll(){
		return Lists.newArrayList(videoRepository.findAll());
	}
	@RequestMapping(method=RequestMethod.POST)
	public @ResponseBody
	Video addVideo(@RequestBody Video video){
		System.out.println("Hi " + video.getName());
		return videoRepository.save(video);
	}

	@RequestMapping(value="/{id}", method=RequestMethod.GET)
	public @ResponseBody
	Video getSingle(@PathVariable Long id){
		return this.GetVideoById(id);
	}

	@RequestMapping(value="/{id}/like", method=RequestMethod.POST)
	public @ResponseBody
	Video like(@PathVariable Long id, Principal p){
		final Video video = this.GetVideoById(id);
		final String username = p.getName();
		boolean notAlreadyLiked = video.getLikedBy().add(username);
		Arrays.stream(video.getLikedBy().toArray()).forEach(val->System.out.println(val + " " + notAlreadyLiked));
		if(!notAlreadyLiked)
			throw new BadRequestException();
		if(notAlreadyLiked)
			video.setLikes(video.getLikes()+1);
		Arrays.stream(video.getLikedBy().toArray()).forEach(val->System.out.println(val + " " + notAlreadyLiked));
		videoRepository.save(video);
		return video;
	}

	@RequestMapping(value="/{id}/unlike", method=RequestMethod.POST)
	public @ResponseBody
	Video unlike(@PathVariable Long id, Principal p){
		final Video video = this.GetVideoById(id);
		final String username = p.getName();
		boolean previouslyLiked = video.getLikedBy().remove(username);
		Arrays.stream(video.getLikedBy().toArray()).forEach(val->System.out.println(val + " " + previouslyLiked));
		if(!previouslyLiked)
			throw new BadRequestException();
		if(previouslyLiked)
			video.setLikes(video.getLikes()-1);
		Arrays.stream(video.getLikedBy().toArray()).forEach(val->System.out.println(val + " " + previouslyLiked));
		videoRepository.save(video);
		return video;
	}

	@RequestMapping(value="/search/findByName",method = RequestMethod.GET)
	public @ResponseBody
	Collection<Video> findByNames(@RequestParam(value = "title") String name){
		return videoRepository.findByName(name);
	}

	@RequestMapping(value="/search/findByDurationLessThan",method = RequestMethod.GET)
	public @ResponseBody
	Collection<Video> findByDurationLessThan(@RequestParam long duration){
		return videoRepository.findByDurationLessThan(duration);
	}

	private Video GetVideoById(final long id){
		Video video = videoRepository.findOne(id);
		if(video == null) throw new NotFoundException();
		return video;
	}
}
