
const shapes = [
    '#', // pressed
    '*', '*', '*', '*', '*', "'", ".", ",", "`"
];

export default class SnowFlakes {
    width: number;
    height: number;
    scaleFactorH: number;
    scaleFactorV: number;

    setupViewport(width: number, height: number, scaleFactorH : number, scaleFactorV : number) {
        this.width = width;
        this.height = height;
        this.scaleFactorH = scaleFactorH;
        this.scaleFactorV = scaleFactorV;
    }

    renderFlakeShape(ctx : CanvasRenderingContext2D, shapeIdx : number, x : number, y : number) {
        ctx.fillStyle = 'white';
        ctx.font = this.scaleFactorV + "px Courier New";
        ctx.fillText(shapes[shapeIdx], x, y + this.scaleFactorV);
    }

}