package com.matchimi;

import java.util.ArrayList;
import java.util.List;

public class ProfileModel {
	
	private String email;
	private String first_name;
	private String last_name;
	private String profile_pic;
	private String gender;
	private String dob;
	private String phone_num;
	private String address;
	private String post_code;
	private String ic_no;
	private String ic_type;
	private String ic_type_id;
	private String ic_back_picture;
	private String ic_front_picture;
	private String ic_expired;
	private List<Integer> skill;
	private String work_exp;
	private String school;
	private String card_number;
	private String card_picture;
	private String ec_name;
	private String ec_email;
	private String ec_phone;
	private String ec_address;
	private String ec_post_code;
	private String ec_relationship;
	private String bank_name;
	private String bank_acc_type;
	private String bank_acc_branch;
	private String bank_acc_number;
	private String bank_statement;
	private String visume;
	
	public ProfileModel() {
		super();
		this.email = null;
		this.first_name = null;
		this.last_name = null;
		this.profile_pic = null;
		this.gender = null;
		this.dob = null;
		this.phone_num = null;
		this.address = null;
		this.post_code = null;
		this.ic_no = null;
		this.ic_type = null;
		this.ic_type_id = null;
		this.ic_back_picture = null;
		this.ic_front_picture = null;
		this.ic_expired = null;
		this.skill = new ArrayList<Integer>();
		this.work_exp = null;
		this.school = null;
		this.card_number = null;
		this.card_picture = null;
		this.ec_name = null;
		this.ec_email = null;
		this.ec_phone = null;
		this.ec_address = null;
		this.ec_post_code = null;
		this.ec_relationship = null;
		this.bank_name = null;
		this.bank_acc_type = null;
		this.bank_acc_branch = null;
		this.bank_acc_number = null;
		this.bank_statement = null;
		this.visume = null;
	}
	
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getFirst_name() {
		return first_name;
	}
	public void setFirst_name(String first_name) {
		this.first_name = first_name;
	}
	public String getLast_name() {
		return last_name;
	}
	public void setLast_name(String last_name) {
		this.last_name = last_name;
	}
	public String getProfile_pic() {
		return profile_pic;
	}
	public void setProfile_pic(String profile_pic) {
		this.profile_pic = profile_pic;
	}
	public String getGender() {
		return gender;
	}
	public void setGender(String gender) {
		this.gender = gender;
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
		if(phone_num == "null") {
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
		if(ic_no == "null") {
			ic_no = null;
		}
		this.ic_no = ic_no;
	}
	public String getIc_type() {
		return ic_type;
	}
	public void setIc_type(String ic_type) {
		this.ic_type = ic_type;
	}
	public String getIc_type_id() {
		return ic_type_id;
	}
	public void setIc_type_id(String ic_type_id) {
		this.ic_type_id = ic_type_id;
	}
	public String getIc_back_picture() {
		return ic_back_picture;
	}
	public void setIc_back_picture(String ic_back_picture) {
		this.ic_back_picture = ic_back_picture;
	}
	public String getIc_front_picture() {
		return ic_front_picture;
	}
	public void setIc_front_picture(String ic_front_picture) {
		this.ic_front_picture = ic_front_picture;
	}
	public String getIc_expired() {
		return ic_expired;
	}
	
	public void setIc_expired(String ic_expired) {
		if(ic_expired == "null") {
			ic_expired = null;
		}
		this.ic_expired = ic_expired;
	}
	
	public List<Integer> getSkill() {
		return skill;
	}
	public void setSkill(List<Integer> skill) {
		this.skill = skill;
	}
	public String getWork_exp() {
		return work_exp;
	}
	public void setWork_exp(String work_exp) {
		if(work_exp == "null") {
			work_exp = null;
		}
		this.work_exp = work_exp;
	}
	public String getSchool() {
		return school;
	}
	
	public void setSchool(String school) {
		if(school == "null") {
			school = null;
		}
		this.school = school;
	}
	
	public String getCard_number() {
		return card_number;
	}
	public void setCard_number(String card_number) {
		if(card_number == "null") {
			card_number = null;
		}
		this.card_number = card_number;
	}
	public String getCard_picture() {
		return card_picture;
	}
	public void setCard_picture(String card_picture) {
		this.card_picture = card_picture;
	}
	public String getEc_name() {
		return ec_name;
	}
	public void setEc_name(String ec_name) {
		this.ec_name = ec_name;
	}
	public String getEc_email() {
		return ec_email;
	}
	public void setEc_email(String ec_email) {
		this.ec_email = ec_email;
	}
	public String getEc_phone() {
		return ec_phone;
	}
	public void setEc_phone(String ec_phone) {
		this.ec_phone = ec_phone;
	}
	public String getEc_address() {
		return ec_address;
	}
	public void setEc_address(String ec_address) {
		this.ec_address = ec_address;
	}
	public String getEc_post_code() {
		return ec_post_code;
	}
	public void setEc_post_code(String ec_post_code) {
		this.ec_post_code = ec_post_code;
	}
	public String getEc_relationship() {
		return ec_relationship;
	}
	public void setEc_relationship(String ec_relationship) {
		this.ec_relationship = ec_relationship;
	}
	public String getBank_name() {
		return bank_name;
	}
	public void setBank_name(String bank_name) {
		this.bank_name = bank_name;
	}
	public String getBank_acc_type() {
		return bank_acc_type;
	}
	public void setBank_acc_type(String bank_acc_type) {
		this.bank_acc_type = bank_acc_type;
	}
	public String getBank_acc_branch() {
		return bank_acc_branch;
	}
	public void setBank_acc_branch(String bank_acc_branch) {
		this.bank_acc_branch = bank_acc_branch;
	}
	public String getBank_acc_number() {
		return bank_acc_number;
	}
	public void setBank_acc_number(String bank_acc_number) {
		this.bank_acc_number = bank_acc_number;
	}
	
	public String getBank_statement() {
		return bank_statement;
	}
	
	public void setBank_statement(String bank_statement) {
		if(bank_statement == "null") {
			bank_statement = null;
		}
		this.bank_statement = bank_statement;
	}
	
	public String getVisume() {
		return visume;
	}
	
	public void setVisume(String visume) {
		if(visume == "null") {
			visume = null;
		}
		this.visume = visume;
	}
	
	
	public boolean checkComplete() {	
		if(this.first_name == null ||
//			this.profile_pic == null ||
			this.gender == null ||
			this.dob == null ||
			this.phone_num == null ||
			this.ic_no == null ||
			this.ic_type == null ||
			this.ic_type_id == null ||
			this.ic_back_picture == null ||
			this.ic_front_picture == null ||
			this.skill == null ||
			this.work_exp == null ||
			this.ec_name == null ||
			this.ec_phone == null ||
			this.ec_relationship == null ||
			this.visume == null	
//			|| this.bank_name == null ||
//			this.bank_acc_branch == null ||
//			this.bank_acc_number == null
			) {
			return false;
		} else if(this.first_name.length() == 0||
//				this.profile_pic.length() == 0||
				this.gender.length() == 0||
				this.dob.length() == 0||
				this.phone_num.length() == 0||
				this.ic_no.length() == 0||
				this.ic_type.length() == 0||
				this.ic_type_id.length() == 0||
				this.ic_back_picture.length() == 0||
				this.ic_front_picture.length() == 0||
				this.work_exp.length() == 0||
				this.ec_name.length() == 0||
				this.ec_phone.length() == 0||
				this.ec_relationship.length() == 0 ||
				this.visume.length() == 0
//				||this.bank_name.length() == 0||
//				this.bank_acc_branch.length() == 0||
//				this.bank_acc_number.length() == 0
				) {
				return false;
		} else {
			return true;
		}
	}

	@Override
	public String toString() {
		return "ProfileModel [email=" + email + ", first_name=" + first_name
				+ ", last_name=" + last_name + ", profile_pic=" + profile_pic
				+ ", gender=" + gender + ", dob=" + dob + ", phone_num="
				+ phone_num + ", address=" + address + ", post_code="
				+ post_code + ", ic_no=" + ic_no + ", ic_type=" + ic_type
				+ ", ic_type_id=" + ic_type_id + ", ic_back_picture="
				+ ic_back_picture + ", ic_front_picture=" + ic_front_picture
				+ ", ic_expired=" + ic_expired + ", skill=" + skill
				+ ", work_exp=" + work_exp + ", school=" + school
				+ ", card_number=" + card_number + ", card_picture="
				+ card_picture + ", ec_name=" + ec_name + ", ec_email="
				+ ec_email + ", ec_phone=" + ec_phone + ", ec_address="
				+ ec_address + ", ec_post_code=" + ec_post_code
				+ ", ec_relationship=" + ec_relationship + ", bank_name="
				+ bank_name + ", bank_acc_type=" + bank_acc_type
				+ ", bank_acc_branch=" + bank_acc_branch + ", bank_acc_number="
				+ bank_acc_number + ", bank_statement=" + bank_statement + 
				", visume=" + visume +"]";
	}
}
