$ErrorActionPreference = 'Stop'

Add-Type -AssemblyName System.Drawing

$root = Split-Path -Parent $PSScriptRoot
$outDir = Join-Path $root 'tmp_2_2_diagrams'
if (Test-Path $outDir) {
    Remove-Item -Recurse -Force $outDir
}
New-Item -ItemType Directory -Path $outDir | Out-Null

function New-Box {
    param(
        [int]$X,
        [int]$Y,
        [int]$W,
        [int]$H,
        [string]$Text,
        [int]$FontSize = 15
    )
    [pscustomobject]@{
        X = $X
        Y = $Y
        W = $W
        H = $H
        Text = $Text
        FontSize = $FontSize
    }
}

function Draw-Diagram {
    param(
        [string]$FileName,
        [string]$Title,
        [object[]]$Boxes,
        [object[]]$Lines
    )

    $bitmap = New-Object System.Drawing.Bitmap(1600, 980)
    $graphics = [System.Drawing.Graphics]::FromImage($bitmap)
    $graphics.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
    $graphics.TextRenderingHint = [System.Drawing.Text.TextRenderingHint]::AntiAliasGridFit
    $graphics.Clear([System.Drawing.Color]::FromArgb(250, 251, 253))

    $titleFont = New-Object System.Drawing.Font('Arial', 26, [System.Drawing.FontStyle]::Bold)
    $titleBrush = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::FromArgb(40, 64, 95))
    $bodyBrush = [System.Drawing.Brushes]::Black
    $centerFormat = New-Object System.Drawing.StringFormat
    $centerFormat.Alignment = [System.Drawing.StringAlignment]::Center
    $centerFormat.LineAlignment = [System.Drawing.StringAlignment]::Center

    $graphics.DrawString($Title, $titleFont, $titleBrush, (New-Object System.Drawing.RectangleF(0, 16, 1600, 50)), $centerFormat)

    $linePen = New-Object System.Drawing.Pen([System.Drawing.Color]::FromArgb(95, 124, 160), 4)
    $arrow = New-Object System.Drawing.Drawing2D.AdjustableArrowCap(6, 8, $true)
    $linePen.CustomEndCap = $arrow
    foreach ($line in $Lines) {
        $graphics.DrawLine($linePen, [int]$line.X1, [int]$line.Y1, [int]$line.X2, [int]$line.Y2)
    }

    foreach ($box in $Boxes) {
        $x = [int]$box.X
        $y = [int]$box.Y
        $w = [int]$box.W
        $h = [int]$box.H
        $rect = New-Object System.Drawing.Rectangle($x, $y, $w, $h)
        $fillBrush = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::FromArgb(234, 243, 255))
        $borderPen = New-Object System.Drawing.Pen([System.Drawing.Color]::FromArgb(47, 93, 138), 3)
        $graphics.FillRectangle($fillBrush, $rect)
        $graphics.DrawRectangle($borderPen, $rect)

        $font = New-Object System.Drawing.Font('Arial', ([float]$box.FontSize), [System.Drawing.FontStyle]::Regular)
        $textRect = New-Object System.Drawing.RectangleF(($x + 10), ($y + 8), ($w - 20), ($h - 16))
        $graphics.DrawString($box.Text, $font, $bodyBrush, $textRect, $centerFormat)

        $font.Dispose()
        $fillBrush.Dispose()
        $borderPen.Dispose()
    }

    $bitmap.Save((Join-Path $outDir $FileName), [System.Drawing.Imaging.ImageFormat]::Png)

    $linePen.Dispose()
    $arrow.Dispose()
    $titleFont.Dispose()
    $titleBrush.Dispose()
    $graphics.Dispose()
    $bitmap.Dispose()
}

Draw-Diagram -FileName 'system_overview.png' -Title 'System Overview' -Boxes @(
    (New-Box 620 90 360 84 'InkForge System'),
    (New-Box 80 280 250 84 'User Management'),
    (New-Box 380 280 250 84 'Creation Management'),
    (New-Box 680 280 250 84 'Community Interaction'),
    (New-Box 980 280 250 84 'AI Assistance'),
    (New-Box 1280 280 250 84 'Admin Management')
) -Lines @(
    @{X1=800;Y1=174;X2=205;Y2=280},
    @{X1=800;Y1=174;X2=505;Y2=280},
    @{X1=800;Y1=174;X2=805;Y2=280},
    @{X1=800;Y1=174;X2=1105;Y2=280},
    @{X1=800;Y1=174;X2=1405;Y2=280}
)

Draw-Diagram -FileName 'user_module.png' -Title 'User Management Module' -Boxes @(
    (New-Box 620 100 360 84 'User Management'),
    (New-Box 80 300 220 76 'Register'),
    (New-Box 340 300 220 76 'Login'),
    (New-Box 600 300 220 76 'Profile Edit'),
    (New-Box 860 300 220 76 'Password Reset'),
    (New-Box 1120 300 220 76 'Email Binding'),
    (New-Box 1380 300 160 76 'Logout'),
    (New-Box 620 520 360 76 'User Center / Profile')
) -Lines @(
    @{X1=800;Y1=184;X2=190;Y2=300},
    @{X1=800;Y1=184;X2=450;Y2=300},
    @{X1=800;Y1=184;X2=710;Y2=300},
    @{X1=800;Y1=184;X2=970;Y2=300},
    @{X1=800;Y1=184;X2=1230;Y2=300},
    @{X1=800;Y1=184;X2=1460;Y2=300},
    @{X1=800;Y1=184;X2=800;Y2=520}
)

Draw-Diagram -FileName 'creation_module.png' -Title 'Creation Management Module' -Boxes @(
    (New-Box 620 100 360 84 'Creation Management'),
    (New-Box 100 300 220 76 'Create Article'),
    (New-Box 360 300 220 76 'Edit Article'),
    (New-Box 620 300 220 76 'Save Draft'),
    (New-Box 880 300 220 76 'Publish Article'),
    (New-Box 1140 300 220 76 'Manage Articles'),
    (New-Box 620 520 360 76 'Novel Chapter Management')
) -Lines @(
    @{X1=800;Y1=184;X2=210;Y2=300},
    @{X1=800;Y1=184;X2=470;Y2=300},
    @{X1=800;Y1=184;X2=730;Y2=300},
    @{X1=800;Y1=184;X2=990;Y2=300},
    @{X1=800;Y1=184;X2=1250;Y2=300},
    @{X1=800;Y1=184;X2=800;Y2=520}
)

Draw-Diagram -FileName 'interaction_module.png' -Title 'Community Interaction Module' -Boxes @(
    (New-Box 620 100 360 84 'Community Interaction'),
    (New-Box 60 300 220 76 'Home Browse'),
    (New-Box 320 300 220 76 'Category Browse'),
    (New-Box 580 300 220 76 'Content Detail'),
    (New-Box 840 300 220 76 'Like / Comment'),
    (New-Box 1100 300 220 76 'Favorite / Shelf'),
    (New-Box 1360 300 180 76 'Follow / Fans')
) -Lines @(
    @{X1=800;Y1=184;X2=170;Y2=300},
    @{X1=800;Y1=184;X2=430;Y2=300},
    @{X1=800;Y1=184;X2=690;Y2=300},
    @{X1=800;Y1=184;X2=950;Y2=300},
    @{X1=800;Y1=184;X2=1210;Y2=300},
    @{X1=800;Y1=184;X2=1450;Y2=300}
)

Draw-Diagram -FileName 'ai_module.png' -Title 'AI Assistance Module' -Boxes @(
    (New-Box 620 100 360 84 'AI Assistance'),
    (New-Box 80 300 220 76 'Summary'),
    (New-Box 340 300 220 76 'AI Continue'),
    (New-Box 600 300 220 76 'AI Polish'),
    (New-Box 860 300 220 76 'Keyword / Mood / Style'),
    (New-Box 1120 300 220 76 'Q & A'),
    (New-Box 620 520 360 76 'Browser Extension Help')
) -Lines @(
    @{X1=800;Y1=184;X2=190;Y2=300},
    @{X1=800;Y1=184;X2=450;Y2=300},
    @{X1=800;Y1=184;X2=710;Y2=300},
    @{X1=800;Y1=184;X2=970;Y2=300},
    @{X1=800;Y1=184;X2=1230;Y2=300},
    @{X1=800;Y1=184;X2=800;Y2=520}
)

Draw-Diagram -FileName 'admin_module.png' -Title 'Admin Management Module' -Boxes @(
    (New-Box 620 100 360 84 'Admin Management'),
    (New-Box 120 300 220 76 'User Admin'),
    (New-Box 380 300 220 76 'Content Admin'),
    (New-Box 640 300 220 76 'Tag Admin'),
    (New-Box 900 300 220 76 'Comment Admin'),
    (New-Box 1160 300 280 76 'AI Statistics')
) -Lines @(
    @{X1=800;Y1=184;X2=230;Y2=300},
    @{X1=800;Y1=184;X2=490;Y2=300},
    @{X1=800;Y1=184;X2=750;Y2=300},
    @{X1=800;Y1=184;X2=1010;Y2=300},
    @{X1=800;Y1=184;X2=1300;Y2=300}
)

Write-Output $outDir
