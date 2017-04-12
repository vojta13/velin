function updateStats(){
    $.ajax({
      url: "stats",
    }).done(function(response) {
      response = JSON.parse(response);
      $.each(response.data, function (idx, item){
        var element =  $("#"+item.id);
        var widthPercentage =  Math.min(item.value / element.attr("aria-valuemax") * 100, 100);

        element.css("width", widthPercentage + "%")
        element.text(item.value)
        element.attr("class","");
        if(widthPercentage > 70){
            element.attr("class","progress-bar progress-bar-danger")
        } else if (widthPercentage > 40){
            element.attr("class","progress-bar progress-bar-warning")
        } else if (widthPercentage < 0) {
            element.attr("class","progress-bar progress-bar-danger");
            element.css("width", "100%");
            element.text("N/A");
        } else {
            element.attr("class","progress-bar progress-bar-success")
        }

      });
    });
}

function updateHealth(){
    $.ajax({
      url: "health",
    }).done(function(response) {
      response = JSON.parse(response);
      $.each(response.data, function (idx, item){
        var element =  $("#"+item.id);
        var health =  item.value;
        element.attr("class","");
        if(health){
            element.attr("class","label label-success")
        } else {
            element.attr("class","label label-danger")
        }
      });
    });
}

setInterval(updateStats, 2000);
setInterval(updateHealth, 2000);