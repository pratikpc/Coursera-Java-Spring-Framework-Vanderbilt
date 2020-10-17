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
package org.magnum.dataup;

import org.magnum.dataup.model.Video;
import org.magnum.dataup.model.VideoStatus;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
class ResourceNotFoundException extends RuntimeException {

}

@Controller
public class VideoController {

    /**
     * You will need to create one or more Spring controllers to fulfill the
     * requirements of the assignment. If you use this file, please rename it
     * to something other than "AnEmptyController"
     * <p>
     * <p>
     * ________  ________  ________  ________          ___       ___  ___  ________  ___  __
     * |\   ____\|\   __  \|\   __  \|\   ___ \        |\  \     |\  \|\  \|\   ____\|\  \|\  \
     * \ \  \___|\ \  \|\  \ \  \|\  \ \  \_|\ \       \ \  \    \ \  \\\  \ \  \___|\ \  \/  /|_
     * \ \  \  __\ \  \\\  \ \  \\\  \ \  \ \\ \       \ \  \    \ \  \\\  \ \  \    \ \   ___  \
     * \ \  \|\  \ \  \\\  \ \  \\\  \ \  \_\\ \       \ \  \____\ \  \\\  \ \  \____\ \  \\ \  \
     * \ \_______\ \_______\ \_______\ \_______\       \ \_______\ \_______\ \_______\ \__\\ \__\
     * \|_______|\|_______|\|_______|\|_______|        \|_______|\|_______|\|_______|\|__| \|__|
     */
    public static final String VIDEO_SVC_PATH = "/video";
    public static final String VIDEO_DATA_PATH = VIDEO_SVC_PATH + "/{id}/data";
    private static AtomicLong currentVideoId = new AtomicLong(1);
    private Map<Long, Video> videos = new HashMap<>();

    /**
     * This endpoint in the API returns a list of the videos that have
     * been added to the server. The Video objects should be returned as
     * JSON.
     * <p>
     * To manually test this endpoint, run your server and open this URL in a browser:
     * http://localhost:8080/video
     *
     * @return
     */
    @RequestMapping(value = VIDEO_SVC_PATH, method = RequestMethod.GET)
    public @ResponseBody
    Collection<Video> getVideoList() {
        return videos.values();
    }

    /**
     * This endpoint allows clients to add Video objects by sending POST requests
     * that have an application/json body containing the Video object information.
     *
     * @return
     */
    @RequestMapping(value = VIDEO_SVC_PATH, method = RequestMethod.POST)
    public @ResponseBody
    Video addVideoMetadata(@RequestBody Video v) {
        v.setId(currentVideoId.getAndIncrement());
        v.setDataUrl(this.getDataUrl(v.getId()));
        videos.put(v.getId(), v);
        return v;
    }

    /**
     * This endpoint allows clients to set the mpeg video data for previously
     * added Video objects by sending multipart POST requests to the server.
     * The URL that the POST requests should be sent to includes the ID of the
     * Video that the data should be associated with (e.g., replace {id} in
     * the url /video/{id}/data with a valid ID of a video, such as /video/1/data
     * -- assuming that "1" is a valid ID of a video).
     *
     * @return
     */
    @RequestMapping(value = VIDEO_DATA_PATH, method = RequestMethod.POST)
    public @ResponseBody
    VideoStatus addVideoData(@PathVariable("id") long id, @RequestParam MultipartFile data) throws IOException {
        final Video v = videos.get(id);
        System.out.println(v);
        System.out.println("$4");
        // if (v == null) throw new ResourceNotFoundException();
        System.out.println(v);
        VideoFileManager manager = VideoFileManager.get();
        try {
            manager.saveVideoData(v, data.getInputStream());
        }catch (Exception ex){throw new ResourceNotFoundException();}
        System.out.println("44");
        return new VideoStatus(VideoStatus.VideoState.READY);
    }

    /**
     * This endpoint should return the video data that has been associated with
     * a Video object or a 404 if no video data has been set yet. The URL scheme
     * is the same as in the method above and assumes that the client knows the ID
     * of the Video object that it would like to retrieve video data for.
     * <p>
     * This method uses Retrofit's @Streaming annotation to indicate that the
     * method is going to access a large stream of data (e.g., the mpeg video
     * data on the server). The client can access this stream of data by obtaining
     * an InputStream from the Response as shown below:
     * <p>
     * VideoSvcApi client = ... // use retrofit to create the client
     * Response response = client.getData(someVideoId);
     * InputStream videoDataStream = response.getBody().in();
     *
     * @param id
     * @return
     */
    @RequestMapping(value = VIDEO_DATA_PATH, method = RequestMethod.GET)
    public void getData(@PathVariable("id") long id, HttpServletResponse response) throws IOException {
        final Video video = videos.get(id);
        if (video == null) {
            throw new ResourceNotFoundException();
        }
        VideoFileManager manager = VideoFileManager.get();
        manager.copyVideoData(video,
                response.getOutputStream());
    }


    private String getDataUrl(long videoId) {
        String url = getUrlBaseForLocalServer() + "/video/" + videoId + "/data";
        return url;
    }

    private String getUrlBaseForLocalServer() {
        HttpServletRequest request =
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String base =
                "http://" + request.getServerName()
                        + ":" + request.getServerPort();
        return base;
    }
}
