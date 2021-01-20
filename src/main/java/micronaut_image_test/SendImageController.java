package micronaut_image_test;

import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.multipart.CompletedFileUpload;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;

import javax.inject.Inject;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.UUID;

@Controller("/image")
public class SendImageController {

    @Inject
    S3Service s3Service;

    @Secured(SecurityRule.IS_ANONYMOUS)
    @Post(value = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA,
            processes = MediaType.APPLICATION_JSON
    )
    public HttpResponse saveImage(
            CompletedFileUpload file
    ) throws IOException {

        String fileKey = UUID
                .randomUUID()
                .toString();
        String key;

        int lastIndexOf = file.getFilename().lastIndexOf(".");
        String ext =  file.getFilename().substring(lastIndexOf + 1);
        key =  fileKey + "." + ext;

        File tempFile;
        try {
            tempFile = File.createTempFile(
                    file.getFilename(),
                    "temp"
            );
        } catch (IOException e) {
            return HttpResponse.status(HttpStatus.INTERNAL_SERVER_ERROR, "couldn't read file");
        }

        Files.copy(
                file.getInputStream(),
                tempFile.toPath(),
                StandardCopyOption.REPLACE_EXISTING
        );

        try {
                ObjectMetadata objectMetadata = new ObjectMetadata();
                System.out.println(file.getContentType().toString());
                objectMetadata.setContentType(file.getContentType().toString());
                objectMetadata.setContentLength(file.getSize());

                InputStream is = new FileInputStream(tempFile);
                PutObjectRequest request = new PutObjectRequest(
                        System.getenv("S3_BUCKET"),
                        key,
                        is,
                        objectMetadata
                ).withCannedAcl(CannedAccessControlList.PublicRead); // <5>
                s3Service.s3client.putObject(request);
                is.close();

            HashMap<String, String> result = new HashMap<>();
            result.put("key", key);

            return HttpResponse.accepted();

        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            return HttpResponse.status(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "exception thrown");
        }
    }

}
