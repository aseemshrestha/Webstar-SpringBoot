package com.webstar.models;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.Size;

import com.webstar.util.Constants;
import com.webstar.util.TimeLapse;

@Entity
@Table( indexes = { @Index( name = "IDX_USER_CATEGORY", columnList = "category" ),
        @Index( name = "IDX_USER_SUBMISSIONS", columnList = "USER_ID" ) } )
public class UserSubmissions
{
    @Id
    @GeneratedValue( strategy = GenerationType.AUTO )
    private Long Id;

    @Size( min = 1, max = 200, message = "required." )
    private String contents;

    @Size( min = 1, max = 50, message = "required." )
    private String category;

    @Size( min = 1, max = 50, message = "required." )
    private String subcategory;

    private String videoUrl;

    private String imageUrl;

    private String Ip;
    @Temporal( TemporalType.TIMESTAMP )

    private Date submittiedDate;
    @Temporal( TemporalType.TIMESTAMP )

    private Date updatedDate;
    @Temporal( TemporalType.TIMESTAMP )

    private Date deletedDate;

    private int isActivePost;

    @ManyToOne
    @JoinColumn( name = "USER_ID" )
    private UserDetails userDetails;

    private int totalComments;

    private int totalLikes;

    private int totalReposts;

    private double avgRatings;

   
    public UserSubmissions()
    {}

    public Long getId()
    {
        return Id;
    }

    public void setId(Long id)
    {
        Id = id;
    }

    public String getContents()
    {
        return contents;
    }

    public void setContents(String contents)
    {
        this.contents = contents;
    }

    public String getCategory()
    {
        return category;
    }

    public void setCategory(String category)
    {
        this.category = category;
    }

    public String getSubcategory()
    {
        return subcategory;
    }

    public void setSubcategory(String subcategory)
    {
        this.subcategory = subcategory;
    }

    public String getVideoUrl()
    {
        String videoId = "";
        if (videoUrl != null && !videoUrl.isEmpty()) {
            if (videoUrl.contains("youtube")) {
                if (videoUrl.contains("?v=")) {
                    videoId = "$$-" + videoUrl.substring(videoUrl.indexOf("?v=") + 3, videoUrl.length());
                }
                if (videoUrl.contains("/embed/")) {
                    videoId = "$$-" + videoUrl.split("/embed/")[1];
                }
            }
            if (videoUrl.contains("vimeo")) {
                videoId = "##-" + videoUrl.split("vimeo.com/")[1];
            }
        }
        return videoId;
    }

    public void setVideoUrl(String videoUrl)
    {
        this.videoUrl = videoUrl;
    }

    public String getImageUrl()
    {
        if (imageUrl != null) {
            int index = imageUrl.indexOf(Constants.IMG_PATH_FOLDER);
            int l = imageUrl.length();
            return imageUrl.substring(index, l).replace("\\", "/");
        }
        return imageUrl;
    }

    public void setImageUrl(String imageUrl)
    {
        this.imageUrl = imageUrl;
    }

    public String getIp()
    {
        return Ip;
    }

    public void setIp(String ip)
    {
        Ip = ip;
    }

    public Date getSubmittiedDate()
    {
        return submittiedDate;
    }

    public String getTimeLapse() throws ParseException
    {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-mm-dd HH:mm:ss");
        Date dateNow = new Date();

        String n = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(dateNow);
        String n1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(getSubmittiedDate());
        return TimeLapse.toRelative(format.parse(n1), format.parse(n));

    }

    public void setSubmittiedDate(Date submittiedDate)
    {
        this.submittiedDate = submittiedDate;
    }

    public Date getUpdatedDate()
    {
        return updatedDate;
    }

    public void setUpdatedDate(Date updatedDate)
    {
        this.updatedDate = updatedDate;
    }

    public Date getDeletedDate()
    {
        return deletedDate;
    }

    public void setDeletedDate(Date deletedDate)
    {
        this.deletedDate = deletedDate;
    }

    public int getIsActivePost()
    {
        return isActivePost;
    }

    public void setIsActivePost(int isActivePost)
    {
        this.isActivePost = isActivePost;
    }

    public UserDetails getUserDetails()
    {
        return userDetails;
    }

    public void setUserDetails(UserDetails userDetails)
    {
        this.userDetails = userDetails;
    }

    public int getTotalComments()
    {
        return totalComments;
    }

    public void setTotalComments(int totalComments)
    {
        this.totalComments = totalComments;
    }

    public int getTotalLikes()
    {
        return totalLikes;
    }

    public void setTotalLikes(int totalLikes)
    {
        this.totalLikes = totalLikes;
    }

    public int getTotalReposts()
    {
        return totalReposts;
    }

    public void setTotalReposts(int totalReposts)
    {
        this.totalReposts = totalReposts;
    }

    public double getAvgRatings()
    {
        NumberFormat formatter = new DecimalFormat("#0.00");
        return Double.parseDouble(formatter.format(avgRatings));
    }

    public void setAvgRatings(double avgRatings)
    {
        this.avgRatings = avgRatings;
    }

}
