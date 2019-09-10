package com.ibm.tfb.ext.util;

import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONObject;

import com.ibm.dpft.engine.core.dbo.DPFTDbo;
import com.ibm.dpft.engine.core.dbo.DPFTDboSet;
import com.ibm.dpft.engine.core.dbo.ResFileDataLayoutDbo;
import com.ibm.dpft.engine.core.dbo.ResFileDataLayoutDetailDboSet;
import com.ibm.dpft.engine.core.exception.DPFTInvalidSystemSettingException;
import com.ibm.dpft.engine.core.exception.DPFTRuntimeException;
import com.ibm.dpft.engine.core.util.DPFTFileReader;

public class LotteryResDataFileReader extends DPFTFileReader {

	public LotteryResDataFileReader(String dir, ResFileDataLayoutDbo resFileDataLayoutDbo, String chal_name) {
		super(dir, resFileDataLayoutDbo, chal_name);
	}

	@Override
	public void write2TargetTable(String timestamp) throws DPFTRuntimeException {
		if (layout == null) {
			throw new DPFTInvalidSystemSettingException("SYSTEM", "DPFT0033E");
		}

		ResFileDataLayoutDetailDboSet layout_detail = layout.getLayoutDetail();
		if (layout_detail == null) {
			Object[] params = { layout.getString("data_layout_id") };
			throw new DPFTInvalidSystemSettingException("SYSTEM", "DPFT0031E", params);
		}

		DPFTDboSet targetSet = layout.getTargetTableDboSet();
		HashMap<String, String> f_col_2_tgt_col_map = layout.getFileColumns2TargetColumnsMapping();
		for (HashMap<String, String> rowdata : read_data) {

			DPFTDbo new_data = targetSet.add();
			new_data.setValue("DATA_DT", timestamp);
			new_data.setValue("CAMP_CODE", rowdata.get("CAMPAIGN_CODE"));
			new_data.setValue("OFFR_CODE", rowdata.get("OFFER_CODE"));
			new_data.setValue("WBA_ID", rowdata.get("_id"));

			for (String col : rowdata.keySet()) {
				if (f_col_2_tgt_col_map.get(col) == null)
					continue;
				new_data.setValue(f_col_2_tgt_col_map.get(col), rowdata.get(col));
			}

			// parse Json
			JSONObject obj = new JSONObject(rowdata.get("structure"));
			JSONArray ans_array = obj.getJSONArray("answer");

			int ans_len = ans_array.length();
			String[] questions = new String[ans_len]; // 依序存問題
			String[] answers = new String[ans_len]; // 依序存答案

			for (int i = 0; i < ans_len; i++) {
				parseJsonToArray(ans_array, questions, answers, i);
				new_data.setValue("FIELD" + (i + 1) + "_NAME", questions[i]);
				new_data.setValue("FIELD" + (i + 1) + "_VALUE", answers[i]);
			}
		}
		targetSet.save();
	}

	private void parseJsonToArray(JSONArray ans_array, String[] questions, String[] answers, int i) {
		JSONObject obj1 = ans_array.getJSONObject(i);
		String type = obj1.getString("type");

		// 單行文字
		if (type.equals("single-line-text")) {
			questions[i] = obj1.getString("name");
			answers[i] = obj1.getString("answer");
		}

//      多行文字
		if (type.equals("multi-line-text")) {
			questions[i] = obj1.getString("name");
			answers[i] = obj1.getString("answer");
		}

//      單選題
		if (type.equals("single-choice")) {
			questions[i] = obj1.getString("name");
			answers[i] = obj1.getString("answer");
		}

//      複選題
		if (type.equals("multi-choice")) {
			JSONArray choice_array = obj1.getJSONArray("answer");
			int choice_len = choice_array.length();
			String tempanswer = "";

			for (int j = 0; j < choice_len; j++) {
				JSONObject choicetemp = choice_array.getJSONObject(j);
				Boolean checked = choicetemp.getBoolean("checked");
				if (checked.equals(true)) {
					tempanswer = tempanswer + choicetemp.getString("optionName");
				}
				if (j < choice_len - 1) {
					tempanswer = tempanswer + " | ";
				}
			}

			questions[i] = obj1.getString("name");
			answers[i] = tempanswer;
		}

//      線性評分
		if (type.equals("linear-score")) {
			questions[i] = obj1.getString("name");
			answers[i] = obj1.getString("answer");
		}

//      下拉式
		if (type.equals("select-choice")) {
			questions[i] = obj1.getString("name");
			answers[i] = obj1.getString("answer");
		}

//      日期 
		if (type.equals("date-enter")) {
//        	使用getString去讀取obj1內Answer的String
			String answer = obj1.getString("answer");
			JSONObject temp = new JSONObject(answer);
			String dateContent = temp.getString("answer");
			Boolean timeActive = temp.getBoolean("timeActive");

//        	若timeActive為True，抓「日期＋時間」
			if (timeActive) {
				String[] dateAndtime = dateContent.split("T");
				String[] date = dateAndtime[0].split("-");
				String[] time = dateAndtime[1].split(":");
				String datetemp = date[0] + date[1] + date[2] + time[0] + time[1] + time[2].substring(0, 1);
				answers[i] = datetemp;
			}
//        	若timeActive為False，抓「日期」
			else {
				String[] date = dateContent.split("-");
				String datetemp = date[0] + date[1] + date[2];
				answers[i] = datetemp;
			}

//        	將結果加入矩陣中
			questions[i] = obj1.getString("name");

		}

//      時間 
		if (type.equals("time-enter")) {
			String answer = obj1.getString("answer");

			String[] dateAndtime = answer.split("T");
			String[] time = dateAndtime[1].split(":");
			String datetemp = time[0] + time[1] + time[2].substring(0, 2);

//        	將結果加入矩陣中
			answers[i] = datetemp;
			questions[i] = obj1.getString("name");

		}

//      矩陣式
		if (type.equals("matrix-choice")) {

//        	確認answer裡面有幾個row
			int rowNum = obj1.getJSONObject("answer").length();
			String rowsListContent = obj1.getString("rowsList");
			JSONArray rowsList = new JSONArray(rowsListContent);

			String answertemp = "";
			String questiontemp = "";

			for (int j = 0; j < rowNum; j++) {
//        		記錄一題問題及答案
				JSONObject rowNameContent = rowsList.getJSONObject(j);
				String rowName = rowNameContent.getString("rowName");
				String rowAnswer = obj1.getJSONObject("answer").getString("row-" + j);

				questiontemp = questiontemp + rowName;
				answertemp = answertemp + rowAnswer;

//        		用「｜」區隔題目與題目
				if (j < rowNum - 1) {
					answertemp = answertemp + " | ";
					questiontemp = questiontemp + " | ";
				}
			}

//        	將結果加入矩陣中
			answers[i] = answertemp;
			questions[i] = questiontemp;
		}

//      手式選答
		if (type.equals("touch-choice")) {
			questions[i] = obj1.getString("name");
			answers[i] = obj1.getString("answer");
		}

//      手勢評分
		if (type.equals("touch-score")) {
			questions[i] = obj1.getString("name");
			answers[i] = obj1.getJSONObject("answer").getString("optionName");
		}

//      已詳閱內容並同意
		if (type.equals("check-box")) {
			questions[i] = obj1.getString("name");
			answers[i] = obj1.getString("answer");
		}

//      固定答案
		if (type.equals("hidden")) {
			questions[i] = obj1.getString("name");
			answers[i] = obj1.getString("answer");
		}
	}
}
