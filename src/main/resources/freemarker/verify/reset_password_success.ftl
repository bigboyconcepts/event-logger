<!DOCTYPE html>
<html>
   <head>
      <title>Floorball</title>
      <meta charset="utf-8">
      <meta name="viewport" content="width=device-width, initial-scale=1">
      <link href="https://fonts.googleapis.com/css?family=Roboto+Condensed" rel="stylesheet">
      <meta http-equiv="X-UA-Compatible" content="IE=edge" />
      <style type="text/css">
         body, table, td, a{-webkit-text-size-adjust: 100%; -ms-text-size-adjust: 100%;} /* Prevent WebKit and Windows mobile changing default text sizes */
         table, td{mso-table-lspace: 0pt; mso-table-rspace: 0pt;} /* Remove spacing between tables in Outlook 2007 and up */
         img{-ms-interpolation-mode: bicubic;} /* Allow smoother rendering of resized image in Internet Explorer */
         img{border: 0; height: auto; line-height: 100%; outline: none; text-decoration: none;}
         table{border-collapse: collapse !important;}
         body{height: 100% !important; margin: 0 !important; padding: 0 !important; width: 100% !important;}
         a[x-apple-data-detectors] {
         color: inherit !important;
         text-decoration: none !important;
         font-size: inherit !important;
         font-family: 'Roboto Condensed', sans-serif; !important;
         font-weight: inherit !important;
         line-height: inherit !important;
         }
         @media screen and (max-width: 525px) {
         .wrapper {
         width: 100% !important;
         max-width: 100% !important;
         }
         .logo img {
         margin: 0 auto !important;
         }
         .mobile-hide {
         display: none !important;
         }
         .img-max {
         max-width: 100% !important;
         width: 100% !important;
         height: auto !important;
         }
         .responsive-table {
         width: 100% !important;
         }
         .padding {
         padding: 10px 5% 15px 5% !important;
         }
         .padding-meta {
         padding: 30px 5% 0px 5% !important;
         text-align: center;
         }
         .no-padding {
         padding: 0 !important;
         }
         .section-padding {
         padding: 50px 15px 50px 15px !important;
         }
         /* ADJUST BUTTONS ON MOBILE */
         .mobile-button-container {
         margin: 0 auto;
         width: 100% !important;
         }
         .mobile-button {
         padding: 15px !important;
         border: 0 !important;
         font-size: 16px !important;
         display: block !important;
         }
         }
         div[style*="margin: 16px 0;"] { margin: 0 !important; }
      </style>
   </head>
   <body style="margin: 0 !important; padding: 0 !important;">
      <table border="0" cellpadding="0" cellspacing="0" width="100%">
         <tr>
            <td bgcolor="#527acc" align="center">
               <table border="0" cellpadding="0" cellspacing="0" width="100%" style="max-width: 500px;" class="wrapper">
                  <tr>
                     <td align="center" valign="top" style="padding: 15px 0;" class="logo">               
                        <img alt="Logo" src="${base_url}logo-1.png" width="120" height="60" style="display: block; font-family: Helvetica, Arial, sans-serif; color: #ffffff; font-size: 16px;" border="0">
                     </td>
                  </tr>
               </table>
            </td>
         </tr>
         <tr>
            <td bgcolor="#ffffff" align="center" style="padding: 70px 15px 70px 15px;" class="section-padding">
               <table border="0" cellpadding="0" cellspacing="0" width="100%" style="max-width: 500px;" class="responsive-table">
                  <tr>
                     <td>
                        <table width="100%" border="0" cellspacing="0" cellpadding="0">
                           <tr>
                              <td class="padding" align="center">
                                 <img src="${base_url}hero-1.jpg" width="500" height="400" border="0" alt="Floorball logo" style="display: block; color: #666666;  font-family: Roboto Condensed, Helvetica, arial, sans-serif; font-size: 16px;" class="img-max">
                              </td>
                           </tr>
                           <tr>
                              <td>
                                 <table width="100%" border="0" cellspacing="0" cellpadding="0">
                                    <tr>
                                       <td align="center" style="font-size: 25px; font-family: Roboto Condensed, Helvetica, Arial, sans-serif; color: #333333; padding-top: 30px;" class="padding">Hi ${member.getFullName(true)}</td>
                                    </tr>
                                    <tr>
                                       <td align="center" style="padding: 20px 0 0 0; font-size: 16px; line-height: 25px; font-family: Roboto Condensed, Helvetica, Arial, sans-serif; color: #666666;" class="padding">Password reset successful.<br>Check your email for your new password.</td>
                                    </tr>
                                 </table>
                              </td>
                           </tr>
                        </table>
                     </td>
                  </tr>
               </table>
            </td>
         </tr>
      </table>
   </body>
</html>