package nz.ac.auckland.cer.project.util;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;

import nz.ac.auckland.cer.common.util.TemplateEmail;
import nz.ac.auckland.cer.project.pojo.Project;
import nz.ac.auckland.cer.project.pojo.ProjectRequest;
import nz.ac.auckland.cer.project.pojo.ProjectWrapper;
import nz.ac.auckland.cer.project.pojo.ResearchOutput;
import nz.ac.auckland.cer.project.pojo.Researcher;
import nz.ac.auckland.cer.project.pojo.survey.Bigger;
import nz.ac.auckland.cer.project.pojo.survey.Faster;
import nz.ac.auckland.cer.project.pojo.survey.More;
import nz.ac.auckland.cer.project.pojo.survey.Survey;

public class EmailUtil {

    private Logger log = Logger.getLogger(EmailUtil.class.getName());
    @Autowired private TemplateEmail templateEmail;
    @Autowired private AffiliationUtil affUtil;
    private Resource projectRequestEmailBodyResource;
    private Resource projectRequestWithSuperviserEmailBodyResource;
    private Resource membershipRequestEmailBodyResource;
    private Resource otherAffiliationEmailBodyResource;
    private Resource newFollowUpEmailBodyResource;
    private Resource newResearchOutputEmailBodyResource;
    private Resource surveyNoticeBodyResource;
    private String projectBaseUrl;
    private String projectRequestEmailSubject;
    private String membershipRequestEmailSubject;
    private String otherAffiliationEmailSubject;
    private String newFollowUpEmailSubject;
    private String newResearchOutputEmailSubject;
    private String surveyNoticeEmailSubject;
    private String emailFrom;
    private String emailTo;
    private String replyTo;

    public void sendProjectRequestEmail(
            Project p,
            String researcherName) throws Exception {

        Map<String, String> templateParams = new HashMap<String, String>();
        templateParams.put("__RESEARCHER_NAME__", researcherName);
        templateParams.put("__PROJECT_TITLE__", p.getName());
        templateParams.put("__PROJECT_DESCRIPTION__", p.getDescription());
        templateParams.put("__PROJECT_LINK__", this.projectBaseUrl + "?id=" + p.getProjectId());
        try {
            this.templateEmail.sendFromResource(this.emailFrom, this.emailTo, null, null,
                    this.projectRequestEmailSubject, this.projectRequestEmailBodyResource, templateParams);
        } catch (Exception e) {
            log.error("Failed to send project request email", e);
            throw new Exception("Failed to notify CeR staff about the new project request");
        }
    }

    public void sendProjectRequestWithSuperviserEmail(
            Project p,
            ProjectRequest pr,
            Researcher superviser,
            String researcherName) throws Exception {

        Map<String, String> templateParams = new HashMap<String, String>();
        String extraInfos = "The supervisor does not yet exist in the database.";
        if (superviser != null) {
            extraInfos = "The supervisor already exists in the database.";
            templateParams.put("__SUPERVISER_NAME__", superviser.getFullName());
            templateParams.put("__SUPERVISER_EMAIL__", superviser.getEmail());
            templateParams.put("__SUPERVISER_PHONE__", superviser.getPhone());
            templateParams.put("__SUPERVISER_INSTITUTION__", superviser.getInstitution());
            templateParams.put("__SUPERVISER_DIVISION__", superviser.getDivision());
            templateParams.put("__SUPERVISER_DEPARTMENT__", superviser.getDepartment());
        } else {
            templateParams.put("__SUPERVISER_NAME__", pr.getSuperviserName());
            templateParams.put("__SUPERVISER_EMAIL__", pr.getSuperviserEmail());
            templateParams.put("__SUPERVISER_PHONE__", pr.getSuperviserPhone());
            boolean otherAffil = pr.getSuperviserAffiliation().toLowerCase().equals("other");
            templateParams.put("__SUPERVISER_INSTITUTION__", otherAffil ? pr.getSuperviserOtherInstitution()
                    : this.affUtil.getInstitutionFromAffiliationString(pr.getSuperviserAffiliation()));
            templateParams.put(
                    "__SUPERVISER_DIVISION__",
                    otherAffil ? pr.getSuperviserOtherDivision() : this.affUtil.getDivisionFromAffiliationString(pr
                            .getSuperviserAffiliation()));
            templateParams.put("__SUPERVISER_DEPARTMENT__", otherAffil ? pr.getSuperviserOtherDepartment()
                    : this.affUtil.getDepartmentFromAffiliationString(pr.getSuperviserAffiliation()));
        }
        templateParams.put("__RESEARCHER_NAME__", researcherName);
        templateParams.put("__PROJECT_TITLE__", p.getName());
        templateParams.put("__PROJECT_DESCRIPTION__", p.getDescription());
        templateParams.put("__PROJECT_LINK__", this.projectBaseUrl + "?id=" + p.getId());
        templateParams.put("__SUPERVISER_EXTRA_INFOS__", extraInfos);

        try {
            this.templateEmail
                    .sendFromResource(this.emailFrom, this.emailTo, null, null, this.projectRequestEmailSubject,
                            this.projectRequestWithSuperviserEmailBodyResource, templateParams);
        } catch (Exception e) {
            log.error("Failed to send project request email", e);
            throw new Exception("Failed to notify CeR staff about the new project request");
        }
    }

    public void sendMembershipRequestRequestEmail(
            Project p,
            String researcherName) throws Exception {

        Map<String, String> templateParams = new HashMap<String, String>();
        templateParams.put("__RESEARCHER_NAME__", researcherName);
        templateParams.put("__PROJECT_TITLE__", p.getName());
        templateParams.put("__PROJECT_DESCRIPTION__", p.getDescription());
        templateParams.put("__PROJECT_LINK__", this.projectBaseUrl + "?id=" + p.getId());
        try {
            this.templateEmail.sendFromResource(this.emailFrom, this.emailTo, null, null,
                    this.membershipRequestEmailSubject, this.membershipRequestEmailBodyResource, templateParams);
        } catch (Exception e) {
            log.error("Failed to send project membership request email", e);
            throw new Exception("Failed to notify CeR staff about the project membership request");
        }
    }

    public void sendOtherAffiliationEmail(
            String institution,
            String division,
            String department) throws Exception {

        Map<String, String> templateParams = new HashMap<String, String>();
        templateParams.put("__INSTITUTION__", institution);
        templateParams.put("__DIVISION__", division);
        templateParams.put("__DEPARTMENT__", department);
        try {
            this.templateEmail.sendFromResource(this.emailFrom, this.emailTo, null, null,
                    this.otherAffiliationEmailSubject, this.otherAffiliationEmailBodyResource, templateParams);
        } catch (Exception e) {
            log.error("Failed to send other institution email.", e);
            throw new Exception("Failed to notify CeR staff about the other institution.");
        }
    }

    public void sendNewFollowUpEmail(
            String researcherName,
            String followUp,
            Integer projectId) throws Exception {

        Map<String, String> templateParams = new HashMap<String, String>();
        templateParams.put("__RESEARCHER_NAME__", researcherName);
        templateParams.put("__FOLLOW_UP__", followUp);
        templateParams.put("__PROJECT_LINK__", this.projectBaseUrl + "?id=" + projectId);
        try {
            this.templateEmail.sendFromResource(this.emailFrom, this.emailTo, null, null, this.newFollowUpEmailSubject,
                    this.newFollowUpEmailBodyResource, templateParams);
        } catch (Exception e) {
            log.error("Failed to send new followup email.", e);
            throw new Exception("Failed to notify CeR staff about the new feedback.");
        }
    }

    public void sendNewResearchOutputEmail(
            String researcherName,
            String researchOutputType,
            String researchOutputDescription,
            Integer projectId) throws Exception {

        Map<String, String> templateParams = new HashMap<String, String>();
        templateParams.put("__RESEARCHER_NAME__", researcherName);
        templateParams.put("__RESEARCH_OUTPUT_TYPE__", researchOutputType);
        templateParams.put("__RESEARCH_OUTPUT_DESCRIPTION__", researchOutputDescription);
        templateParams.put("__PROJECT_LINK__", this.projectBaseUrl + "?id=" + projectId);
        try {
            this.templateEmail.sendFromResource(this.emailFrom, this.emailTo, null, null,
                    this.newResearchOutputEmailSubject, this.newResearchOutputEmailBodyResource, templateParams);
        } catch (Exception e) {
            log.error("Failed to send new followup email.", e);
            throw new Exception("Failed to notify CeR staff about the new feedback.");
        }
    }

    public void sendSurveyEmail(
            String researcherName,
            ProjectWrapper pw,
            Survey survey) throws Exception {

        Map<String, String> templateParams = new HashMap<String, String>();
        templateParams.put("__RESEARCHER_NAME__", researcherName);
        templateParams.put("__PROJECT_CODE__", pw.getProject().getProjectCode());
        templateParams.put("__PROJECT_TITLE__", pw.getProject().getName());
        templateParams.put("__PROJECT_DESCRIPTION__", pw.getProject().getDescription());
        templateParams.put("__PROJECT_LINK__", this.projectBaseUrl + "?id=" + pw.getProject().getId());
        String perfImp = "";
        Faster faster = survey.getFaster();
        Bigger bigger = survey.getBigger();
        More more = survey.getMore();
        if (faster == null && bigger == null && more == null) {
            perfImp = "N/A";
        } else {
            if (faster != null) {
                perfImp += faster.toString() + " ";
            }
            if (bigger != null) {
                perfImp += bigger.toString() + " ";
            }
            if (more != null) {
                perfImp += more.toString();
            }
        }
        templateParams.put("__PERFORMANCE_IMPROVEMENTS__", perfImp);
        templateParams.put("__FUTURE_NEEDS__", survey.getFutureNeeds().toString());
        String tmp = "";
        Integer noResearchOutput = survey.getResearchOutcome().getNoResearchOutput();
        if (noResearchOutput != null && noResearchOutput > 0) {
            tmp = "N/A";
        } else {
            for (ResearchOutput ro : survey.getResearchOutcome().getResearchOutputs()) {
                if (ro.getDescription() != null && !ro.getDescription().trim().isEmpty()) {
                    tmp += ro.getDescription() + System.getProperty("line.separator")
                            + System.getProperty("line.separator");
                }
            }
        }
        templateParams.put("__RESEARCH_OUTCOME__", tmp.trim());
        templateParams.put("__FEEDBACK__", survey.getFeedback().toString());

        try {
            this.templateEmail.sendFromResource(this.emailFrom, this.emailTo, null, null,
                    this.surveyNoticeEmailSubject, this.surveyNoticeBodyResource, templateParams);
        } catch (Exception e) {
            log.error("Failed to send new followup email.", e);
            throw new Exception("Failed to notify CeR staff about the new feedback.");
        }
    }

    public void setEmailFrom(
            String emailFrom) {

        this.emailFrom = emailFrom;
    }

    public void setEmailTo(
            String emailTo) {

        this.emailTo = emailTo;
    }

    public void setProjectRequestEmailSubject(
            String projectRequestEmailSubject) {

        this.projectRequestEmailSubject = projectRequestEmailSubject;
    }

    public void setProjectRequestEmailBodyResource(
            Resource projectRequestEmailBodyResource) {

        this.projectRequestEmailBodyResource = projectRequestEmailBodyResource;
    }

    public void setMembershipRequestEmailSubject(
            String membershipRequestEmailSubject) {

        this.membershipRequestEmailSubject = membershipRequestEmailSubject;
    }

    public void setMembershipRequestEmailBodyResource(
            Resource membershipRequestEmailBodyResource) {

        this.membershipRequestEmailBodyResource = membershipRequestEmailBodyResource;
    }

    public void setProjectRequestWithSuperviserEmailBodyResource(
            Resource projectRequestWithSuperviserEmailBodyResource) {

        this.projectRequestWithSuperviserEmailBodyResource = projectRequestWithSuperviserEmailBodyResource;
    }

    public void setNewFollowUpEmailBodyResource(
            Resource newFollowUpEmailBodyResource) {

        this.newFollowUpEmailBodyResource = newFollowUpEmailBodyResource;
    }

    public void setNewFollowUpEmailSubject(
            String newFollowUpEmailSubject) {

        this.newFollowUpEmailSubject = newFollowUpEmailSubject;
    }

    public void setOtherAffiliationEmailBodyResource(
            Resource otherAffiliationEmailBodyResource) {

        this.otherAffiliationEmailBodyResource = otherAffiliationEmailBodyResource;
    }

    public void setOtherAffiliationEmailSubject(
            String otherAffiliationEmailSubject) {

        this.otherAffiliationEmailSubject = otherAffiliationEmailSubject;
    }

    public void setNewResearchOutputEmailBodyResource(
            Resource newResearchOutputEmailBodyResource) {

        this.newResearchOutputEmailBodyResource = newResearchOutputEmailBodyResource;
    }

    public void setNewResearchOutputEmailSubject(
            String newResearchOutputEmailSubject) {

        this.newResearchOutputEmailSubject = newResearchOutputEmailSubject;
    }

    public void setProjectBaseUrl(
            String projectBaseUrl) {

        this.projectBaseUrl = projectBaseUrl;
    }

    public void setReplyTo(
            String replyTo) {

        this.replyTo = replyTo;
    }

    public void setSurveyNoticeBodyResource(
            Resource surveyNoticeBodyResource) {

        this.surveyNoticeBodyResource = surveyNoticeBodyResource;
    }

    public void setSurveyNoticeEmailSubject(
            String surveyNoticeEmailSubject) {

        this.surveyNoticeEmailSubject = surveyNoticeEmailSubject;
    }

}
