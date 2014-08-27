(function() {
	"use strict"

	Date.prototype.format = function(formatString) {

		var addPadding = function(value, length) {
			var negative = ((value < 0) ? "-" : "");
			var zeros = "0";
for (var i = 2; i < length; i++) {
zeros += "0";
}
return negative + (zeros + Math.abs(value).toString()).slice(-length);
};

var replacements = {
			date : this,
			YYYY : function() {
				return this.date.getFullYear();
			},
			YY : function() {
				return this.date.getFullYear() % 100;
			},
			MMMM : function() {
				return this.date.getMonthName();
			},
			MMM : function() {
				return this.date.getMonthNameShort();
			},
			MM : function() {
				return addPadding((this.date.getMonth() + 1), 2);
			},
			M : function() {
				return this.date.getMonth() + 1;
			},
			DDDD : function() {
				return this.date.getDayName();
			},
			DDD : function() {
				return this.date.getDayNameShort();
			},
			DD : function() {
				return addPadding(this.date.getDate(), 2);
			},
			D : function() {
				return this.date.getDate();
			},
			S : function() {
				return this.date.getDateSuffix();
			},
			HH : function() {
				return addPadding(this.date.getHours(), 2);
			},
			H : function() {
				return this.date.getHours();
			},
			hh : function() {
				var hour = this.date.getHours();
				if (hour > 12) {
					hour -= 12;
				} else if (hour < 1) {
					hour = 12;
				}
				return addPadding(hour, 2);
			},
			h : function() {
				var hour = this.date.getHours();
				if (hour > 12) {
					hour -= 12;
				} else if (hour < 1) {
					hour = 12;
				}
				return hour;
			},
			mm : function() {
				return addPadding(this.date.getMinutes(), 2);
			},
			m : function() {
				return this.date.getMinutes();
			},
			ss : function() {
				return addPadding(this.date.getSeconds(), 2);
			},
			s : function() {
				return this.date.getSeconds();
			},
			fff : function() {
				return addPadding(this.date.getMilliseconds(), 3);
			},
			ff : function() {
				return addPadding(Math.floor(this.date.getMilliseconds() / 10),
						2);
			},
			f : function() {
				return Math.floor(this.date.getMilliseconds() / 100);
			},
			zzzz : function() {
				return addPadding(Math
						.floor(-this.date.getTimezoneOffset() / 60), 2)
						+ ":"
						+ addPadding(-this.date.getTimezoneOffset() % 60, 2);
			},
			zzz : function() {
				return Math.floor(-this.date.getTimezoneOffset() / 60) + ":"
						+ addPadding(-this.date.getTimezoneOffset() % 60, 2);
			},
			zz : function() {
				return addPadding(Math
						.floor(-this.date.getTimezoneOffset() / 60), 2);
			},
			z : function() {
				return Math.floor(-this.date.getTimezoneOffset() / 60);
			},
			tt : function() {
				return this.date.getMeridiem(true);
			},
			TT : function() {
				return this.date.getMeridiem(false);
			}
		};

		var formats = new Array();
		while (formatString.length > 0) {
			if (formatString[0] == "\"") {
				var temp = /"[^"]*"/m.exec(formatString);
				if (temp == null) {
					formats.push(formatString.substring(1));
					formatString = "";
				} else {
					temp = temp[0].substring(1, temp[0].length - 1);
					formats.push(temp);
					formatString = formatString.substring(temp.length + 2);
				}
			} else if (formatString[0] == "'") {
				var temp = /'[^']*'/m.exec(formatString);
				if (temp == null) {
					formats.push(formatString.substring(1));
					formatString = "";
				} else {
					temp = temp[0].substring(1, temp[0].length - 1);
					formats.push(temp);
					formatString = formatString.substring(temp.length + 2);
				}
			} else if (formatString[0] == "\\") {
				if (formatString.length > 1) {
					formats.push(formatString.substring(1, 2));
					formatString = formatString.substring(2);
				} else {
					formats.push("\\");
					formatString = "";
				}
			} else {
				var foundMatch = false;
				for (var i = formatString.length; i > 0; i--) {
					if (formatString.substring(0, i) in replacements) {
						formats.push(replacements[formatString.substring(0, i)]
								());
						formatString = formatString.substring(i);
						foundMatch = true;
						break;
					}
				}
				if (!foundMatch) {
					formats.push(formatString[0]);
					formatString = formatString.substring(1);
				}
			}
		}

		return formats.join("");
	};
})();