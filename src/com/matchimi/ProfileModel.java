package com.matchimi;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;

public class ProfileModel {

//	private String first_name;
//	private String last_name;

	private String full_name;	
	private String email;	
	private String profile_pic;
	private String gender;
	private String gender_id;
	private String dob;
	private String phone_num;
	private String address;
	private String post_code;
	
	private String ic_no;
	private String ic_type;
	private String ic_type_id;
	private String ic_back_picture;
	private String ic_front_picture;

//	private List<Integer> skill;
	private String work_exp;
	private String school;
	private String student_front_picture;
	private String student_back_picture;

	private String ec_name;
	private String ec_email;
	private String ec_phone;
	private String ec_address;
	private String ec_post_code;
	private String ec_relationship;

	private String bank_name;
	private String bank_id;
	private String bank_account_name;
	private String bank_branch_name;
	private String bank_account_no;
	private String bank_statement;

	private Boolean is_student;
	private String visume;
	private String visume_file;
	
	public ProfileModel() {
		super();

		this.email = null;
		this.full_name = null;
		this.profile_pic = null;
		this.gender = null;
		this.gender_id = null;
		this.dob = null;
		this.phone_num = null;
		this.address = null;
		this.post_code = null;
		this.work_exp = null;
		
		this.ic_no = null;
		this.ic_type = null;
		this.ic_type_id = null;
		this.ic_back_picture = null;
		this.ic_front_picture = null;
		
		this.ec_name = null;
		this.ec_email = null;
		this.ec_phone = null;
		this.ec_address = null;
		this.ec_post_code = null;
		this.ec_relationship = null;
		
		this.bank_id = null;
		this.bank_name = null;
		this.bank_account_name = null;
		this.bank_account_no = null;
		this.bank_branch_name = null;
		this.bank_statement = null;
		
		this.is_student = null;
		this.school = null;
		this.student_front_picture = null;
		this.student_back_picture = null;
		
		this.visume = null;
		this.visume_file = null;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getFullName() {
		return full_name;
	}

	public void setFullName(String full_name) {
		this.full_name = full_name;
	}

	public String getProfile_pic() {
		return profile_pic;
	}

	public void setProfile_pic(String profile_pic) {
		if (profile_pic == "null") {
			profile_pic = null;
		}
		this.profile_pic = profile_pic;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}
	
	public String getGenderID() {
		return gender_id;
	}

	public void setGenderID(String gender_id) {
		this.gender_id = gender_id;
	}


	public Boolean getIsStudent() {
		return is_student;
	}

	public void setStudent(String is_student) {
		Log.d(CommonUtilities.TAG, "Set student " + is_student);
		
		if(is_student.equals("true")) {
			this.is_student = true;
		} else {
			this.is_student = false;
		}
	}

	public String getDob() {
		return dob;
	}

	public void setDob(String dob) {
		this.dob = dob;
	}

	public String getPhone_num() {
		return phone_num;
	}

	public void setPhone_num(String phone_num) {
		if (phone_num == "null") {
			phone_num = null;
		}
		this.phone_num = phone_num;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getPost_code() {
		return post_code;
	}

	public void setPost_code(String post_code) {
		this.post_code = post_code;
	}

	public String getIc_no() {
		return ic_no;
	}

	public void setIc_no(String ic_no) {
		if (ic_no == "null") {
			ic_no = null;
		}
		this.ic_no = ic_no;
	}

	public String getIc_type() {
		return ic_type;
	}

	public void setIc_type(String ic_type) {
		if (ic_type == "null") {
			ic_type = null;
		}
		this.ic_type = ic_type;
	}

	public String getIc_type_id() {
		return ic_type_id;
	}

	public void setIc_type_id(String ic_type_id) {
		if (ic_type_id == "null") {
			ic_type_id = null;
		}
		this.ic_type_id = ic_type_id;
	}

	public String getIc_back_picture() {
		return ic_back_picture;
	}

	public void setIc_back_picture(String ic_back_picture) {
		if (ic_back_picture == "null") {
			ic_back_picture = null;
		}
		this.ic_back_picture = ic_back_picture;
	}

	public String getIc_front_picture() {
		return ic_front_picture;
	}

	public void setIc_front_picture(String ic_front_picture) {
		if (ic_front_picture == "null") {
			ic_front_picture = null;
		}
		
		this.ic_front_picture = ic_front_picture;
	}

//	public List<Integer> getSkill() {
//		return skill;
//	}
//
//	public void setSkill(List<Integer> skill) {
//		this.skill = skill;
//	}

	public String getWork_exp() {
		return work_exp;
	}

	public void setWork_exp(String work_exp) {
		if (work_exp == "null") {
			work_exp = null;
		}
		this.work_exp = work_exp;
	}

	public String getSchool() {
		return school;
	}

	public void setSchool(String school) {
		if (school == "null") {
			school = null;
		}
		this.school = school;
	}

	public String getStudentBackCard_picture() {
		return student_back_picture;
	}

	public void setStudentBackCard_picture(String student_back_picture) {
		if (student_back_picture == "null") {
			student_back_picture = null;
		}
		this.student_back_picture = student_back_picture;
	}
	
	public String getStudentFrontCard_picture() {
		return student_front_picture;
	}

	public void setStudentFrontCard_picture(String student_front_picture) {
		if (student_front_picture == "null") {
			student_front_picture = null;
		}
		this.student_front_picture = student_front_picture;
	}

	public String getEc_name() {
		return ec_name;
	}

	public void setEc_name(String ec_name) {
		if (ec_name == "null") {
			ec_name = null;
		}
		this.ec_name = ec_name;
	}

	public String getEc_email() {
		return ec_email;
	}

	public void setEc_email(String ec_email) {
		if (ec_email == "null") {
			ec_email = null;
		}
		this.ec_email = ec_email;
	}

	public String getEc_phone() {
		return ec_phone;
	}

	public void setEc_phone(String ec_phone) {
		if (ec_phone == "null") {
			ec_phone = null;
		}
		this.ec_phone = ec_phone;
	}

	public String getEc_address() {
		return ec_address;
	}

	public void setEc_address(String ec_address) {
		if (ec_address == "null") {
			ec_address = null;
		}
		this.ec_address = ec_address;
	}

	public String getEc_post_code() {
		return ec_post_code;
	}

	public void setEc_post_code(String ec_post_code) {
		if (ec_post_code == "null") {
			ec_post_code = null;
		}
		this.ec_post_code = ec_post_code;
	}

	public String getEc_relationship() {
		return ec_relationship;
	}

	public void setEc_relationship(String ec_relationship) {
		if (ec_relationship == "null") {
			ec_relationship = null;
		}
		this.ec_relationship = ec_relationship;
	}
	
	public String getBank_ID() {
		return bank_id;
	}

	public void setBank_ID(String bank_id) {
		if (bank_id == "null") {
			bank_id = null;
		}
		this.bank_id = bank_name;
	}

	public String getBank_name() {
		return bank_name;
	}

	public void setBank_name(String bank_name) {
		if (bank_name == "null") {
			bank_name = null;
		}
		this.bank_name = bank_name;
	}

	public String getBank_account_name() {
		return bank_account_name;
	}

	public void setBank_account_name(String bank_account_name) {
		if (bank_account_name == "null") {
			bank_account_name = null;
		}
		this.bank_account_name = bank_account_name;
	}

	public String getBank_branch_name() {
		return bank_branch_name;
	}

	public void setBank_branch_name(String bank_branch_name) {
		if (bank_branch_name == "null") {
			bank_branch_name = null;
		}
		this.bank_branch_name = bank_branch_name;
	}

	public String getBank_account_no() {
		return bank_account_no;
	}

	public void setBank_account_no(String bank_account_no) {
		if (bank_account_no == "null") {
			bank_account_no = null;
		}
		this.bank_account_no = bank_account_no;
	}

	public String getBank_statement() {
		return bank_statement;
	}

	public void setBank_statement(String bank_statement) {
		if (bank_statement == "null") {
			bank_statement = null;
		}
		this.bank_statement = bank_statement;
	}

	public String getVisume() {
		return visume;
	}

	public void setVisume(String visume) {
		if (visume == "null") {
			visume = null;
		}
		this.visume = visume;
	}
	
	public String getVisumeFile() {
		return visume_file;
	}

	public void setVisumeFile(String visume_file) {
		if (visume_file == "null") {
			visume_file = null;
		}
		this.visume_file = visume_file;
	}

	public boolean checkComplete() {
		
		
		if (this.full_name == null
				|| this.gender == null 
				|| this.dob == null
				|| this.phone_num == null 
				|| this.ic_type == null 
				|| this.ic_type_id == null
				|| this.ic_back_picture == null
				|| this.ic_front_picture == null
				|| this.work_exp == null 
				|| this.ec_name == null
				|| this.ec_phone == null 
				|| this.is_student == null
				|| this.ec_relationship == null)  {

			return false;
			
		} else if (this.full_name.length() == 0
				|| this.gender.length() == 0 
				|| this.dob.length() == 0
				|| this.phone_num.length() == 0 
				|| this.ic_type.length() == 0 
				|| this.ic_type_id.length() == 0
				|| this.ic_back_picture.length() == 0
				|| this.ic_front_picture.length() == 0
				|| this.work_exp.length() == 0 
				|| this.ec_name.length() == 0
				|| this.ec_phone.length() == 0
				|| this.ec_relationship.length() == 0) {

			return false;
		} else {
			// Check if part-timer is student
			if(this.is_student) {
				if(this.school == null || this.school.length() == 0 ||
					this.student_back_picture == null ||
					this.student_back_picture.length() == 0 ||
					this.student_front_picture == null ||
					this.student_front_picture.length() == 0) {
					
					return false;
				}
			}
			
			return true;
		}
	}

	@Override
	public String toString() {
		return "ProfileModel [email=" + email + ", full_name=" + full_name
				+ ", profile_pic=" + profile_pic
				+ ", gender=" + gender + ", dob=" + dob + ", phone_num="
				+ phone_num + ", address=" + address + ", post_code="
				+ post_code + ", ic_no=" + ic_no + ", ic_type=" + ic_type
				+ ", ic_type_id=" + ic_type_id + ", ic_back_picture="
				+ ic_back_picture + ", ic_front_picture=" + ic_front_picture
				+ ""
				+ ", work_exp=" + work_exp + ", school=" + school
			    + ", ec_name=" + ec_name + ", ec_email="
				+ ec_email + ", ec_phone=" + ec_phone + ", ec_address="
				+ ec_address + ", ec_post_code=" + ec_post_code
				+ ", ec_relationship=" + ec_relationship + ", bank_name="
				+ bank_name + ", bank_id=" + bank_id
				+ ", bank_branch_name=" + bank_branch_name + ", bank_account_no="
				+ bank_account_no + ", bank_statement=" + bank_statement
				+ ", visume=" + visume + "]";
	}
}
