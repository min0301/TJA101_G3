
$(document).ready(function () {

    "use strict";

    /* ==========================================================================
    ApexChart Line Graph
    ========================================================================== */

    var options = {
        series: [{
            name: "Profile Views",
            data: [10, 41, 35, 51, 49, 62, 69, 91, 48]
        }],
        chart: {
            height: 350,
            type: 'line',
            zoom: {
                enabled: false
            }
        },
        dataLabels: { 	
            enabled: false
        },
        stroke: {
            curve: 'straight'
        },
        grid: {
            row: {
                colors: ['transparent', 'transparent'], // takes an array which will be repeated on columns
                opacity: 0.5
            },
        },
        xaxis: {
            categories: ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep'],
        },
        colors: ['#1BC5BD']
    };

    var chart = new ApexCharts(document.querySelector("#chart"), options);
    chart.render();		

  
});