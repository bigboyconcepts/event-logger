package com.newtecsolutions.floorball.model;

import com.newtecsolutions.floorball.FBException;
import com.newtecsolutions.floorball.MyResponse;
import com.newtecsolutions.floorball.utils.ConfigManager;
import com.newtecsolutions.floorball.utils.HibernateUtil;
import com.newtecsolutions.floorball.utils.LocaleManager;
import com.newtecsolutions.floorball.utils.LogUtils;

import org.apache.commons.io.FilenameUtils;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.hibernate.Session;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.skynetsoftware.jutils.RandomString;
import org.skynetsoftware.jutils.StringUtils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import javax.annotation.Nonnull;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import sun.misc.IOUtils;

/**
 * Created by pedja on 6/28/17 1:40 PM.
 * This class is part of the FloorBallBackend
 * Copyright © 2017 ${OWNER}
 */
@Entity
public class File extends HibernateModel
{
    /**
     * Photo thumbnail widths*/
    private static final float[] PHOTO_WIDTHS = {150, 360, 540, 720, 1024};

    private static final java.io.File FILES_DIR = new java.io.File(ConfigManager.getInstance().getString(ConfigManager.CONFIG_SERVER_STATIC_CONTENT_DIR));

    private static final RandomString FILE_NAME_GENERATOR = new RandomString(16);

    static final List<String> IMAGE_EXTENSIONS = Arrays.asList(("bmp,dds,gif,jpg,png,psd,pspimage,tga,thm,tif,tiff," +
            "yuv,abm,afx,cpg,cpt,dcm,dib,dpx,dt2,hdp,ipx,itc2,jp2,jpeg,jps,jpx,max,mng,mpo,mxi," +
            "pictclipping,ppm,psp,pspbrush,pvr,pxm,sdr,sid,skm,thm,tif,wb1,wbc,wbd,wbz,xcf,2bp," +
            "360,accountpicture-ms,acorn,agif,agp,apd,apng,apx,art,asw,avatar,avb,awd,blkrt,bm2," +
            "bmc,bss,can,cd5,cdg,cin,cit,colz,cpc,cps,csf,djvu,dm3,dmi,dtw,dvl,ecw,epp,exr,fits," +
            "fpos,fpx,gbr,gcdp,gih,gim,hdr,hdrp,hpi,i3d,info,ithmb,iwi,j2c,jb2,jbig2,jbr,jia,jng," +
            "jpc,jxr,kdi,lb,lif,lzp,mat,mbm,mix,mnr,mpf,mrxs,msp,myl,ncd,oc3,oc4,oc5,oci,omf,oplc" +
            ",ora,ota,ozb,pano,pat,pbm,pcd,pcx,pdd,pdn,pe4,pe4,pgf,pgm,pi2,pic,pic,picnc,pict," +
            "pixadex,pmg,pnm,pns,pov,ppf,prw,psb,psdx,pse,psf,ptg,px,pxd,pxr,pza,pzp,pzs,qmg,qti," +
            "qtif,ras,rif,rle,rli,rpf,rvg,s2mv,sai,sct,sig,skitch,spa,spe,sph,spj,spp,spr,sup,tbn," +
            "tex,tg4,thumb,tjp,tn,tpf,tps,vpe,vrphoto,vss,wbmp,webp,xpm,zif,73i,8xi,9,png,aic,ais," +
            "apm,aps,awd,bmf,bmx,bmz,brn,brt,bti,c4,cal,cals,cdc,cimg,cpbitmap,cpd,cpx,ct,dc2,dcx," +
            "ddt,dgt,dicom,djv,fax,fil,frm,gfie,ggr,gmbck,gmspr,gp4,gpd,gro,ica,icn,icon,icpr,ilbm," +
            "ink,int,ipick,ivr,j2k,jas,jbf,jfi,jfif,jif,jpd,jpe,jpf,jpg2,jtf,jwl,kic,kpg,lbm,ljp,mac," +
            "mic,msk,ncr,nct,odi,otb,oti,ozj,ozt,pap,pc3,pfi,pfr,pix,pjpg,pm,pni,pnt,pp4,pp5,pts,ptx," +
            "ptx,pwp,pxicon,rcu,rgb,rgf,ric,riff,rri,rsb,rsr,sbp,scn,sfc,sfw,sgi,shg,skypeemoticonset," +
            "sld,sprite,sumo,sun,sva,svm,t2b,tfc,tm2,tub,ufo,uga,vda,vic,viff,vst,wbm,wdp,wi,wpb," +
            "wpe,wvl,xbm,xwd,y,ysp,001,411,8pbs,acr,adc,albm,arr,artwork,arw,blz,brk,cam,ce,cut,ddb," +
            "drz,fac,face,fal,fbm,fpg,g3,gfb,grob,gry,hf,hr,hrf,ic1,ic2,ic3,icb,img,imj,iphotoproject," +
            "ivue,j,jbig,jbmp,jiff,kdk,kfx,kodak,mbm,mcs,met,mip,mrb,neo,nlm,pac,pal,pc1,pc2,pi1," +
            "pi2,pi3,pi4,pi5,pi6,pic,pix,pjpeg,pm3,pntg,pop,pov,ptk,qif,rcl,rgb,rix,rs,sar,scg,sci," +
            "scp,scu,sep,sff,sim,smp,sob,spc,spiff,spu,sr,ste,suniff,taac,tb0,tn1,tn2,tn3,tny,tpi," +
            "trif,u,urt,usertile-ms,v,vff,vna,wic,wmp,ai,eps,ps,svg,asy,cdd,cdmm,cdr,cgm,cvx,drw," +
            "emf,emz,fxg,graffle,hpl,plt,svgz,vsd,vsdx,xar,artb,cdmt,cdmtz,cdmz,cil,clarify,cmx," +
            "csy,cv5,cvg,cvi,dcs,design,dhs,dia,dpp,dpr,drawing,drw,dxb,egc,ep,epsf,ezdraw,fh10," +
            "fh11,fh9,fig,fs,gdraw,gstencil,hgl,hpg,hpgl,idea,igx,lmk,mgcb,mgmf,mgmx,mp,odg,pat,pen," +
            "pl,plt,rdl,scv,sk2,sketch,slddrt,snagitstamps,snagstyles,sxd,tlc,tne,ufr,vbr,vml,vsdm," +
            "vst,vstm,vstx,wmf,wmz,wpg,xmind,xmmap,abc,ac5,ac6,af3,art,awg,cag,ccx,cdt,cdx,cdx,cnv," +
            "cor,cvs,cwt,ddrw,ded,dpx,drawit,dsf,fh6,fh7,fh8,fhd,fif,fmv,ft11,ftn,gem,glox,gls,gsd," +
            "gtemplate,igt,ink,mgc,mgmt,mgs,mgtx,mmat,otg,ovp,ovr,psid,sda,sk1,smf,ssk,std,stn,svf," +
            "tpl,vec,xpr,yal,af2,cxf,fh3,fh4,fh5,ft10,ft7,ft8,ft9,gks,imd,ink,nap,pcs,pd,pfd,pfv," +
            "pmg,pobj,pws,zgm").split(","));
    static final List<String> VIDEO_EXTENSIONS = Arrays.asList(("3g2,3gp,asf,asx,avi,flv,m4v,mov,mp4,mpg,rm,srt,swf,vob," +
            "wmv,aepx,ale,avp,avs,bdm,bik,bin,bsf,camproj,cpi,dash,divx,dmsm,dream,dvdmedia,dvr-ms," +
            "dzm,dzp,edl,f4v,fbr,fcproject,hdmov,imovieproj,ism,ismv,m2p,mkv,mod,moi,mpeg,mts,mxf," +
            "ogv,otrkey,pds,prproj,psh,r3d,rcproject,rmvb,scm,smil,snagproj,sqz,stx,swi,tix,trp,ts," +
            "veg,vf,vro,webm,wlmp,wtv,xvid,yuv,3gp2,3gpp,3p2,890,aaf,aec,aep,aetx,ajp,amc,amv,amx," +
            "arcut,arf,avb,avchd,avv,axm,bdmv,bdt3,bmc,bmk,camrec,ced,cine,cip,clpi,cmmp,cmmtpl," +
            "cmproj,cmrec,cst,d2v,d3v,dat,dce,dck,dcr,dcr,dir,dmsd,dmsd3d,dmss,dmx,dpa,dpg,dv-avi," +
            "dvr,dvx,dxr,dzt,evo,eye,ezt,f4p,fbz,fcp,flc,flh,fli,fpdx,ftc,gcs,gfp,gts,hdv,hkm,ifo," +
            "imovieproject,ircp,ismc,ivr,izz,izzy,jss,jts,jtv,kdenlive,lrv,m1pg,m21,m21,m2t,m2ts," +
            "m2v,mani,mgv,mj2,mjp,mk3d,mnv,mp21,mp21,mpgindex,mpl,mpls,mproj,mpv,mqv,msdvd,mse," +
            "mswmm,mtv,mvd,mve,mvp,mvp,mvy,mxv,ncor,nsv,nuv,nvc,ogm,ogx,pac,pgi,photoshow,piv,plproj," +
            "pmf,ppj,prel,pro,prtl,pxv,qtl,qtz,rcd,rdb,rec,rmd,rmp,rms,roq,rsx,rum,rv,rvid,rvl,sbk," +
            "scc,screenflow,sdv,sedprj,seq,sfvidcap,siv,smi,smi,smk,stl,svi,swt,tda3mt,thp,tivo," +
            "tod,tp,tp0,tpd,tpr,trec,tsp,ttxt,tvlayer,tvs,tvshow,usf,usm,vbc,vc1,vcpf,vcv,vdo,vdr," +
            "vep,vfz,vgz,viewlet,vlab,vp6,vp7,vpj,vsp,wcp,wmd,wmmp,wmx,wp3,wpl,wve,wvx,xej,xel,xesc," +
            "xfl,xlmv,y4m,zm1,zm2,zm3,zmv,264,3gpp2,3mm,60d,aet,avc,avd,avs,awlive,bdt2,bnp,box,bs4," +
            "bu,bvr,byu,camv,clk,cx3,dav,ddat,dif,dlx,dmb,dmsm3d,dnc,dv4,f4f,fbr,ffd,flx,gvp,h264," +
            "inp,int,irf,iva,ivf,jmv,k3g,ktn,lrec,lsx,lvix,m1v,m2a,m4u,meta,mjpg,modd,moff,moov," +
            "movie,mp2v,mp4,infovid,mp4v,mpe,mpl,mpsub,mvc,mvex,mys,osp,par,playlist," +
            "pns,pro4dvd,pro5dvd,proqc,pssd,pva,pvr,qt,qtch,qtindex,qtm,rp,rts,sbt,scn,sfd,sml,smv," +
            "spl,str,tdt,tid,tvrecording,vcr,vem,vft,vfw,vid,video,vix,vs4,vse,w32,wm,wot,xmv,yog," +
            "787,am,anim,aqt,bix,cel,cvc,db2,dsy,gl,gom,grasp,gvi,ismclip,ivs,kmv,lsf,m15,m4e,m75," +
            "mmv,mob,mpeg1,mpeg4,mpf,mpg2,mpv2,msh,mvb,nut,orv,pjs,pmv,psb,rmd,rmv,rts,scm,sec,ssf," +
            "ssm,tdx,vdx,viv,vivo,vp3,zeg").split(","));
    private String mimeType;
    private String fileDir;
    private String fileName;
    private Set<TrackActionRegion> actionRegions;
    private int width, height;
    //private Track track;

    public String getMimeType()
    {
        return mimeType;
    }

    public void setMimeType(String mimeType)
    {
        this.mimeType = mimeType;
    }

    public String getFileDir()
    {
        return fileDir;
    }

    public void setFileDir(String fileDir)
    {
        this.fileDir = fileDir;
    }

    public String getFileName()
    {
        return fileName;
    }

    public void setFileName(String fileName)
    {
        this.fileName = fileName;
    }

    public int getWidth()
    {
        return width;
    }

    public void setWidth(int width)
    {
        this.width = width;
    }

    public int getHeight()
    {
        return height;
    }

    public void setHeight(int height)
    {
        this.height = height;
    }

    @Transient
    public boolean isVideo()
    {
        return isVideo(mimeType);
    }

    @Transient
    public boolean isImage()
    {
        return isImage(mimeType);
    }

    @OneToMany(fetch = FetchType.LAZY, cascade= CascadeType.ALL, mappedBy="video", orphanRemoval = true)
    @OnDelete(action = OnDeleteAction.NO_ACTION)
    public Set<TrackActionRegion> getActionRegions()
    {
        return actionRegions;
    }

    public void setActionRegions(Set<TrackActionRegion> actionRegions)
    {
        this.actionRegions = actionRegions;
    }

    /*@OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "trackImage")
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonIgnore
    public Track getTrack()
    {
        return track;
    }

    public void setTrack(Track track)
    {
        this.track = track;
    }*/

    /**
     * Check if file is video by mime type. It only check extension though*/
    public static boolean isVideo(String mimeType)
    {
        if (StringUtils.isEmpty(mimeType))
            return false;
        try
        {
            MimeType mt = new MimeType(mimeType);
            return VIDEO_EXTENSIONS.contains(mt.getSubType());
        }
        catch (MimeTypeParseException e)
        {
            LogUtils.getLogger().log(Level.WARNING, e.getMessage(), e);
            return false;
        }
    }


    /**
     * Check if file is image by mime type. It only check extension though*/
    public static boolean isImage(String mimeType)
    {
        if (StringUtils.isEmpty(mimeType))
            return false;
        try
        {
            MimeType mt = new MimeType(mimeType);
            return IMAGE_EXTENSIONS.contains(mt.getSubType());
        }
        catch (MimeTypeParseException e)
        {
            LogUtils.getLogger().log(Level.WARNING, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Check if uploading file is image*/
    public static boolean isImage(FormDataContentDisposition metadata)
    {
        return IMAGE_EXTENSIONS.contains(FilenameUtils.getExtension(metadata.getFileName()));
    }

    /**
     * Check if uploading file is video*/
    public static boolean isVideo(FormDataContentDisposition metadata)
    {
        return VIDEO_EXTENSIONS.contains(FilenameUtils.getExtension(metadata.getFileName()));
    }

    /**
     * Check if uploading file is pdf*/
    public static boolean isPdf(FormDataContentDisposition metadata)
    {
        return "pdf".equalsIgnoreCase(FilenameUtils.getExtension(metadata.getFileName()));
    }

    /**
     * Upload file and create relation to parent model
     * @param parentModel model that this file belongs to*/
    public static File uploadFile(@Nonnull HibernateModel parentModel, @Nonnull InputStream inputStream, @Nonnull FormDataContentDisposition metadata, LocaleManager localeManager)
    {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        try
        {
            File file = new File();
            session.save(file);

            //folder name in format {parent_id}/{file_id}, example: 45/456
            String folder = String.valueOf(parentModel.getId()) + java.io.File.separator + String.valueOf(file.getId());

            //create folder if it doesn't exist
            java.io.File fileDir = new java.io.File(FILES_DIR, folder);
            if (!fileDir.exists())
            {
                boolean created = fileDir.mkdirs();
                if (!created)
                {
                    throw new FBException(MyResponse.ErrorCode.server_error, localeManager.getString("Failed to upload file. Internal server error.", null));
                }
                setPermissions(fileDir);
                setPermissions(fileDir.getParentFile());
            }

            //generate random filename
            String fileName = FILE_NAME_GENERATOR.nextString() + "." + FilenameUtils.getExtension(metadata.getFileName());
            java.io.File out = new java.io.File(fileDir, fileName);
            //if its and image, create thumbnails
            if (isImage(metadata))
            {
                //read image as byte array
                byte[] pixels = IOUtils.readFully(inputStream, -1, false);
                //create OCV MAT from pixels
                Mat matImg = Imgcodecs.imdecode(new MatOfByte(pixels), Imgcodecs.IMREAD_UNCHANGED);

                //write original image to file
                Imgcodecs.imwrite(out.getAbsolutePath(), matImg);

                float origWidth = matImg.width();
                float origHeight = matImg.height();
                file.setWidth((int) origWidth);
                file.setHeight((int) origHeight);

                //create thumbnails
                for (float width : PHOTO_WIDTHS)
                {
                    String thumbName = (int) width + "_" + fileName;
                    java.io.File thumbFile;
                    if (matImg.width() <= width)
                    {
                        Imgcodecs.imwrite((thumbFile = new java.io.File(fileDir.getAbsolutePath(), thumbName)).getAbsolutePath(), matImg);
                    }
                    else
                    {
                        float height = origHeight / (origWidth / width);

                        Size size = new Size(width, height);

                        Mat target = new Mat(size, matImg.type());

                        Imgproc.resize(matImg, target, size);
                        Imgcodecs.imwrite((thumbFile = new java.io.File(fileDir.getAbsolutePath(), thumbName)).getAbsolutePath(), target);
                    }
                    setPermissions(thumbFile);
                }
                file.setMimeType("image/" + FilenameUtils.getExtension(metadata.getFileName()));
            }
            else
            {
                if (isVideo(metadata))
                    file.setMimeType("video/" + FilenameUtils.getExtension(metadata.getFileName()));
                else
                    file.setMimeType("application/octet-stream");
                org.apache.commons.io.IOUtils.copy(inputStream, new FileOutputStream(out));
            }
            setPermissions(out);
            file.setFileDir(folder);
            file.setFileName(fileName);
            session.save(file);
            return file;
        }
        catch (IOException e)
        {
            throw new FBException(MyResponse.ErrorCode.server_error, 500, e.getMessage(), e);
        }
    }

    /**
     * Set Posix style permission to 755, TODO this is platform dependent, works only if server is started on Posix system*/
    private static void setPermissions(java.io.File photoDir) throws IOException
    {
        Set<PosixFilePermission> perms = new HashSet<>();
        perms.add(PosixFilePermission.OWNER_READ);
        perms.add(PosixFilePermission.OWNER_WRITE);
        perms.add(PosixFilePermission.OWNER_EXECUTE);
        perms.add(PosixFilePermission.GROUP_READ);
        perms.add(PosixFilePermission.GROUP_EXECUTE);
        perms.add(PosixFilePermission.OTHERS_READ);
        perms.add(PosixFilePermission.OTHERS_EXECUTE);

        Files.setPosixFilePermissions(photoDir.toPath(), perms);
    }

}
